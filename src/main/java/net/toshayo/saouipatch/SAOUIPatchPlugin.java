package net.toshayo.saouipatch;

import cpw.mods.fml.relauncher.IFMLLoadingPlugin;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.*;

import java.util.Map;

@IFMLLoadingPlugin.MCVersion("1.7.10")
@IFMLLoadingPlugin.TransformerExclusions("net.toshayo.saouipatch")
public class SAOUIPatchPlugin implements IFMLLoadingPlugin, IClassTransformer {
    @Override
    public String[] getASMTransformerClass() {
        return new String[] { getClass().getName() };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        ClassReader classReader = new ClassReader(basicClass);
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        switch(transformedName) {
            case "com.saomc.screens.ingame.IngameGUI":
                // Inject pre overlay render event call
                classReader.accept(new IngameGUIVisitor(classWriter), ClassReader.EXPAND_FRAMES);
                return classWriter.toByteArray();
            case "com.saomc.screens.menu.StartupGUI":
                // Remove dev build warning
                classReader.accept(new StartupGUIVisitor(classWriter), ClassReader.EXPAND_FRAMES);
                return classWriter.toByteArray();
            case "com.saomc.SoundCore":
                // Fix crash with sound playing on logout
                classReader.accept(new SoundCoreVisitor(classWriter), ClassReader.EXPAND_FRAMES);
                return classWriter.toByteArray();
        }
        return basicClass;
    }

    static class IngameGUIVisitor extends ClassVisitor {
        public IngameGUIVisitor(ClassVisitor cv) {
            super(Opcodes.ASM5, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String methodName, String desc, String signature, String[] exceptions) {
            MethodVisitor methodVisitor = cv.visitMethod(access, methodName, desc, signature, exceptions);
            if (methodName.equals("renderHotbar") && desc.equals("(IIF)V")) {
                return new IngameGUIRenderHotbarVisitor(methodVisitor);
            }
            return methodVisitor;
        }

        static class IngameGUIRenderHotbarVisitor extends MethodVisitor {
            public IngameGUIRenderHotbarVisitor(MethodVisitor mv) {
                super(Opcodes.ASM5, mv);
            }

            @Override
            public void visitCode() {
                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETSTATIC, "net/minecraftforge/client/event/RenderGameOverlayEvent$ElementType", "HOTBAR", "Lnet/minecraftforge/client/event/RenderGameOverlayEvent$ElementType;");
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/saomc/screens/ingame/IngameGUI", "pre", "(Lnet/minecraftforge/client/event/RenderGameOverlayEvent$ElementType;)Z", false);

                super.visitCode();
            }
        }
    }

    static class StartupGUIVisitor extends ClassVisitor {
        public StartupGUIVisitor(ClassVisitor cv) {
            super(Opcodes.ASM5, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String methodName, String desc, String signature, String[] exceptions) {
            MethodVisitor methodVisitor = cv.visitMethod(access, methodName, desc, signature, exceptions);
            if(methodName.equals("isDev") && desc.equals("()Z")) {
                methodVisitor.visitCode();
                methodVisitor.visitInsn(Opcodes.ICONST_0);
                methodVisitor.visitInsn(Opcodes.IRETURN);
                methodVisitor.visitMaxs(1, 1);
                methodVisitor.visitEnd();
            }
            return methodVisitor;
        }
    }

    static class SoundCoreVisitor extends ClassVisitor {
        public SoundCoreVisitor(ClassVisitor cv) {
            super(Opcodes.ASM5, cv);
        }

        @Override
        public MethodVisitor visitMethod(int access, String methodName, String desc, String signature, String[] exceptions) {
            MethodVisitor methodVisitor = cv.visitMethod(access, methodName, desc, signature, exceptions);
            if(methodName.equals("play") && desc.equals("(Lnet/minecraft/client/Minecraft;Ljava/lang/String;)V")) {
                return new SoundCorePlayVisitor(methodVisitor);

            }
            return methodVisitor;
        }

        static class SoundCorePlayVisitor extends MethodVisitor {
            private static final String DEOBF_WORLD_FIELD = "theWorld";
            private static final String OBF_WORLD_FIELD = "field_71441_e";

            protected SoundCorePlayVisitor(MethodVisitor mv) {
                super(Opcodes.ASM5, mv);
            }

            @Override
            public void visitCode() {
                Label returnLabel = new Label();
                Label endLabel = new Label();

                mv.visitVarInsn(Opcodes.ALOAD, 0);
                mv.visitFieldInsn(Opcodes.GETFIELD, "net/minecraft/client/Minecraft", SAOUIPatchMod.isObfEnv() ? OBF_WORLD_FIELD : DEOBF_WORLD_FIELD, "Lnet/minecraft/client/multiplayer/WorldClient;");
                mv.visitJumpInsn(Opcodes.IFNULL, returnLabel);

                super.visitCode();

                mv.visitJumpInsn(Opcodes.GOTO, endLabel);

                mv.visitLabel(returnLabel);
                mv.visitInsn(Opcodes.RETURN);

                mv.visitLabel(endLabel);
            }

            @Override
            public void visitEnd() {
                super.visitEnd();
            }
        }
    }
}
