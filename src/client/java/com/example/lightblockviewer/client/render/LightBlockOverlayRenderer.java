package com.example.lightblockviewer.client.render;

import com.example.lightblockviewer.LightBlockViewer;
import com.example.lightblockviewer.client.ClientToggleState;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public class LightBlockOverlayRenderer {

    private static final LightBlockOverlayRenderer INSTANCE = new LightBlockOverlayRenderer();

    private static final int SCAN_RADIUS = 16;

    /** Ticks between rescans while the player stays in the same block, to pick up edited blocks. */
    private static final int RESCAN_INTERVAL_TICKS = 10;

    private static final RenderPipeline LIGHT_OVERLAY_PIPELINE = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath(
                            LightBlockViewer.MOD_ID, "pipeline/light_overlay"))
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .build()
    );

    private static final ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final Vector3f MODEL_OFFSET = new Vector3f();
    private static final Matrix4f TEXTURE_MATRIX = new Matrix4f();

    private final List<BlockPos> lightBlocks = new ArrayList<>();
    private BlockPos scanCenter;
    private int ticksSinceScan;
    private boolean scanDirty = true;

    private BufferBuilder buffer;
    private MappableRingBuffer vertexBuffer;
    private boolean hasVertices = false;
    private boolean closed = false;

    private LightBlockOverlayRenderer() {}

    public static LightBlockOverlayRenderer getInstance() {
        return INSTANCE;
    }

    /**
     * Keeps the cached light block positions up to date. Called once per client tick.
     *
     * <p>The surrounding area is only scanned when the player moves to another block or every
     * {@link #RESCAN_INTERVAL_TICKS} ticks, because scanning it for every frame is far more
     * expensive than reusing the positions.
     */
    public void tick(Minecraft client) {
        if (client.level == null || client.player == null) {
            lightBlocks.clear();
            scanCenter = null;
            scanDirty = true;
            return;
        }

        if (++ticksSinceScan >= RESCAN_INTERVAL_TICKS) {
            scanDirty = true;
        }

        if (!client.player.blockPosition().equals(scanCenter)) {
            scanDirty = true;
        }
    }

    public void onBeforeTranslucent(WorldRenderContext context) {
        if (!ClientToggleState.isEnabled()) return;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || client.player == null) return;

        extract(context, client);
        if (hasVertices) {
            draw(client);
        }
    }

    private void extract(WorldRenderContext context, Minecraft client) {
        if (scanDirty) {
            scan(client);
        }

        PoseStack matrices = context.matrices();
        Vec3 camera = context.worldState().cameraRenderState.pos;

        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        if (buffer == null) {
            buffer = new BufferBuilder(ALLOCATOR,
                    LIGHT_OVERLAY_PIPELINE.getVertexFormatMode(),
                    LIGHT_OVERLAY_PIPELINE.getVertexFormat());
        }

        hasVertices = false;
        Matrix4fc positionMatrix = matrices.last().pose();

        for (BlockPos pos : lightBlocks) {
            BlockState state = client.level.getBlockState(pos);
            if (!(state.getBlock() instanceof LightBlock)) {
                continue;
            }

            int level = state.getValue(LightBlock.LEVEL);
            float brightness = level / 15.0f;
            float red = 1.0f;
            float green = 0.2f + 0.8f * brightness;
            float blue = 0.1f * brightness;
            float alpha = 0.35f;

            renderFilledBox(positionMatrix, buffer,
                    pos.getX() + 0.01f, pos.getY() + 0.01f, pos.getZ() + 0.01f,
                    pos.getX() + 0.99f, pos.getY() + 0.99f, pos.getZ() + 0.99f,
                    red, green, blue, alpha);
            hasVertices = true;
        }

        matrices.popPose();
    }

    private void scan(Minecraft client) {
        lightBlocks.clear();
        BlockPos center = client.player.blockPosition();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int x = -SCAN_RADIUS; x <= SCAN_RADIUS; x++) {
            for (int y = -SCAN_RADIUS; y <= SCAN_RADIUS; y++) {
                for (int z = -SCAN_RADIUS; z <= SCAN_RADIUS; z++) {
                    mutable.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (client.level.getBlockState(mutable).getBlock() instanceof LightBlock) {
                        lightBlocks.add(mutable.immutable());
                    }
                }
            }
        }

        scanCenter = center;
        ticksSinceScan = 0;
        scanDirty = false;
    }

    private void renderFilledBox(Matrix4fc positionMatrix, BufferBuilder buffer,
                                  float minX, float minY, float minZ,
                                  float maxX, float maxY, float maxZ,
                                  float red, float green, float blue, float alpha) {
        // Front face (+Z)
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);

        // Back face (-Z)
        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);

        // Left face (-X)
        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);

        // Right face (+X)
        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);

        // Top face (+Y)
        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);

        // Bottom face (-Y)
        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
    }

    private void draw(Minecraft client) {
        MeshData builtBuffer = buffer.buildOrThrow();
        MeshData.DrawState drawParameters = builtBuffer.drawState();
        VertexFormat format = drawParameters.format();

        int vertexBufferSize = drawParameters.vertexCount() * format.getVertexSize();

        if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
            if (vertexBuffer != null) {
                vertexBuffer.close();
            }
            vertexBuffer = new MappableRingBuffer(
                    () -> LightBlockViewer.MOD_ID + " light overlay",
                    GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE,
                    vertexBufferSize);
        }

        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        try (GpuBuffer.MappedView mappedView = commandEncoder.mapBuffer(
                vertexBuffer.currentBuffer().slice(0, builtBuffer.vertexBuffer().remaining()),
                false, true)) {
            MemoryUtil.memCopy(builtBuffer.vertexBuffer(), mappedView.data());
        }

        GpuBuffer vertices = vertexBuffer.currentBuffer();

        // Handle index buffer
        GpuBuffer indices;
        VertexFormat.IndexType indexType;
        if (LIGHT_OVERLAY_PIPELINE.getVertexFormatMode() == VertexFormat.Mode.QUADS) {
            builtBuffer.sortQuads(ALLOCATOR, RenderSystem.getProjectionType().vertexSorting());
            indices = LIGHT_OVERLAY_PIPELINE.getVertexFormat()
                    .uploadImmediateIndexBuffer(builtBuffer.indexBuffer());
            indexType = builtBuffer.drawState().indexType();
        } else {
            RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer =
                    RenderSystem.getSequentialBuffer(LIGHT_OVERLAY_PIPELINE.getVertexFormatMode());
            indices = shapeIndexBuffer.getBuffer(drawParameters.indexCount());
            indexType = shapeIndexBuffer.type();
        }

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR,
                        MODEL_OFFSET, TEXTURE_MATRIX);

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(
                        () -> LightBlockViewer.MOD_ID + " light overlay rendering",
                        client.getMainRenderTarget().getColorTextureView(),
                        OptionalInt.empty(),
                        client.getMainRenderTarget().getDepthTextureView(),
                        OptionalDouble.empty())) {
            renderPass.setPipeline(LIGHT_OVERLAY_PIPELINE);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indices, indexType);
            renderPass.drawIndexed(0, 0, drawParameters.indexCount(), 1);
        }

        builtBuffer.close();
        vertexBuffer.rotate();
        buffer = null;
    }

    public void close() {
        if (closed) {
            return;
        }
        closed = true;

        buffer = null;
        hasVertices = false;
        lightBlocks.clear();
        scanCenter = null;

        ALLOCATOR.close();
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
    }
}
