package gigaherz.elementsofpower.models;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;
import net.minecraft.client.renderer.block.model.ItemTransformVec3f;
import net.minecraft.client.renderer.block.model.ModelBlock;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.crash.CrashReport;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ReportedException;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.*;
import net.minecraftforge.fml.relauncher.ReflectionHelper;
import org.apache.commons.lang3.tuple.Pair;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

@SuppressWarnings("deprecation")
public class ObjModelLoader implements ICustomModelLoader
{
    public static final ObjModelLoader instance = new ObjModelLoader();

    private final Set<String> enabledDomains = new HashSet<>();
    private final Set<ResourceLocation> explicitOverrides = new HashSet<>();

    final Map<ResourceLocation, IModel> modelCache = new HashMap<>();

    public ObjModelLoader()
    {
        ModelLoaderRegistry.registerLoader(this);
    }

    public void enableDomain(String domain)
    {
        enabledDomains.add(domain.toLowerCase());
    }

    public void setExplicitOverride(ResourceLocation location)
    {
        explicitOverrides.add(location);
    }

    @Override
    public boolean accepts(ResourceLocation modelLocation)
    {
        if (explicitOverrides.contains(modelLocation))
        {
            return true;
        }

        if (!enabledDomains.contains(modelLocation.getResourceDomain()))
            return false;

        // This requires the model reference to include the extension,
        // which doesn't seem to be possible for items
        return modelLocation.getResourcePath().endsWith(".obj");
    }

    @Override
    public IModel loadModel(ResourceLocation modelLocation) throws IOException
    {
        ResourceLocation json, obj;

        if (modelCache.containsKey(modelLocation))
            return modelCache.get(modelLocation);

        if (explicitOverrides.contains(modelLocation))
        {
            json = new ResourceLocation(modelLocation.getResourceDomain(), modelLocation.getResourcePath().substring("models/".length()));
            obj = new ResourceLocation(modelLocation.toString() + ".obj");
        }
        else
        {
            String path = modelLocation.getResourcePath();
            int start = "models/".length();
            int length = path.length() - ".obj".length();

            obj = modelLocation;
            json = new ResourceLocation(
                    modelLocation.getResourceDomain(),
                    path.substring(start, length));
        }

        ObjModel model = ObjModel.loadFromResource(obj);

        model.modelBlock = ObjModelLoader.ModelUtilities.loadJsonModel(json);

        modelCache.put(modelLocation, model);

        return model;
    }

    @Override
    public void onResourceManagerReload(IResourceManager resourceManager)
    {
        modelCache.clear();

        ModelUtilities.clearCache();
    }

    public static class BakedModel
            implements IFlexibleBakedModel, IPerspectiveAwareModel
    {
        final static Matrix4f identity;

        static
        {
            identity = new Matrix4f();
            identity.setIdentity();
        }

        final ImmutableMap<TransformType, Matrix4f> transformations;
        final ImmutableList<BakedQuad> generalQuads;
        final TextureAtlasSprite iconSprite;
        final VertexFormat format;
        final boolean isGui3d;

        public BakedModel(ImmutableList<BakedQuad> generalQuads,
                          ModelBlock modelBlock,
                          TextureAtlasSprite particle,
                          VertexFormat format)
        {
            this.transformations = ObjModelLoader.ModelUtilities.loadModelTransforms(modelBlock);
            this.iconSprite = particle;
            this.generalQuads = generalQuads;
            this.format = format;
            this.isGui3d = !ObjModelLoader.ModelUtilities.getRootLocation(modelBlock).getResourcePath().equals("builtin/generated");
        }

        @Override
        public List<BakedQuad> getFaceQuads(EnumFacing face)
        {
            return Collections.emptyList();
        }

        @Override
        public List<BakedQuad> getGeneralQuads()
        {
            return generalQuads;
        }

        @Override
        public VertexFormat getFormat()
        {
            return format;
        }

        @Override
        public boolean isAmbientOcclusion()
        {
            return false;
        }

        @Override
        public boolean isGui3d()
        {
            return isGui3d;
        }

        @Override
        public boolean isBuiltInRenderer()
        {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleTexture()
        {
            return iconSprite;
        }

        @Override
        public ItemCameraTransforms getItemCameraTransforms()
        {
            return null;
        }

        @Override
        public Pair<? extends IFlexibleBakedModel, Matrix4f> handlePerspective(TransformType cameraTransformType)
        {
            return Pair.of(this, transformations.getOrDefault(cameraTransformType, identity));
        }
    }

    static class ModelUtilities
    {
        static final Hashtable<ResourceLocation, ModelBlock> modelBlocks = new Hashtable<>();

        static void clearCache()
        {
            modelBlocks.clear();
        }

        static ImmutableMap<TransformType, Matrix4f> loadModelTransforms(final ModelBlock modelblock)
        {
            if (modelblock == null)
                return ImmutableMap.of();


            Map<TransformType, Matrix4f> map = new HashMap<>();
            map.put(TransformType.THIRD_PERSON, getMatrixForTransform(modelblock, TransformType.THIRD_PERSON));
            map.put(TransformType.FIRST_PERSON, getMatrixForTransform(modelblock, TransformType.FIRST_PERSON));
            map.put(TransformType.HEAD, getMatrixForTransform(modelblock, TransformType.HEAD));
            map.put(TransformType.GUI, getMatrixForTransform(modelblock, TransformType.GUI));
            map.put(TransformType.NONE, getMatrixForTransform(modelblock, TransformType.NONE));

            return Maps.immutableEnumMap(map);
        }

        static Matrix4f getMatrixForTransform(ModelBlock b, ItemCameraTransforms.TransformType t)
        {
            try
            {
                Method m = ReflectionHelper.findMethod(ModelBlock.class, b, new String[]{"func_181681_a"}, ItemCameraTransforms.TransformType.class);
                m.setAccessible(true);

                ItemTransformVec3f tt = (ItemTransformVec3f)m.invoke(b, t);
                return getMatrix(tt);
            }
            catch(InvocationTargetException | IllegalAccessException e)
            {
                throw new ReportedException(new CrashReport("Error obtaining camera transform", e));
            }
        }

        static Matrix4f getMatrix(ItemTransformVec3f transform)
        {
            javax.vecmath.Matrix4f m = new javax.vecmath.Matrix4f(), t = new javax.vecmath.Matrix4f();
            m.setIdentity();
            m.setTranslation(new Vector3f( transform.translation.x,  transform.translation.y,  transform.translation.z));
            t.setIdentity();
            t.rotY((float) Math.toRadians(transform.rotation.y));
            m.mul(t);
            t.setIdentity();
            t.rotX((float) Math.toRadians(transform.rotation.x));
            m.mul(t);
            t.setIdentity();
            t.rotZ((float) Math.toRadians(transform.rotation.z));
            m.mul(t);
            t.setIdentity();
            t.m00 = transform.scale.x;
            t.m11 = transform.scale.y;
            t.m22 = transform.scale.z;
            m.mul(t);
            return m;
        }

        static ModelBlock loadJsonModel(final ResourceLocation loc)
        {
            ModelBlock modelBlock = modelBlocks.get(loc);

            if (modelBlock != null)
            {
                return modelBlock;
            }

            if (loc.getResourcePath().startsWith("builtin/"))
                return null;

            try
            {
                IResource iresource = Minecraft.getMinecraft().getResourceManager().getResource(getJsonLocation(loc));
                if (iresource != null)
                {
                    java.io.Reader reader = new InputStreamReader(iresource.getInputStream(), Charsets.UTF_8);

                    modelBlock = ModelBlock.deserialize(reader);
                    modelBlock.name = loc.toString();
                    modelBlocks.put(loc, modelBlock);

                    ResourceLocation parentLoc = modelBlock.getParentLocation();
                    if (parentLoc != null && !parentLoc.getResourcePath().startsWith("builtin/"))
                    {
                        ModelBlock parentModel = loadJsonModel(parentLoc);
                        if (parentModel != null)
                        {
                            modelBlock.getParentFromMap(modelBlocks);
                        }
                    }
                }
            }
            catch (IOException e)
            {
                throw new ReportedException(new CrashReport("Exception loading JSON Model: " + loc.toString(), e));
            }

            return modelBlock;
        }

        static ResourceLocation getJsonLocation(ResourceLocation loc)
        {
            return new ResourceLocation(loc.getResourceDomain(), "models/" + loc.getResourcePath() + ".json");
        }

        public static ResourceLocation getRootLocation(ModelBlock modelBlock)
        {
            while (modelBlock.parent != null)
                modelBlock = modelBlock.parent;
            return modelBlock.getParentLocation();
        }
    }
}