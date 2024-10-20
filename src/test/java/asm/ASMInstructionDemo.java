package asm;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceMethodVisitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * 这个例子是对asm tree指令的输出和输入测试
 * @author Liubsyy
 * @date 2024/10/20
 **/
public class ASMInstructionDemo {

    public static void testMethod() {
        System.out.println("Hello");

        int a = 5;
        int b = 10;
        int c = a+b;

        try{
            int m = 1/0;
        }catch (Exception e){
            throw new RuntimeException(e);
        }

        System.out.println(c);

        //TableSwitchInsnNode指令，连续case
        int incr = (int)(Math.random()*10);
        switch (incr){
            case 1: incr++;break;
            case 2: incr+=2;break;
            case 3: incr+=3;break;
            case 4: incr+=4;break;
            case 5: incr+=5;break;
        }

        //LookupSwitchInsnNode指令，非连续的case就会出现这个
        switch (incr) {
            case 10:
                System.out.println("Ten");
                break;
            case 20:
                System.out.println("Twenty");
                break;
            default:
                System.out.println("Default");
                break;
        }

        //InvokeDynamicInsnNode指令
        Runnable r = () -> System.out.println("Hello, Lambda!");
        r.run();

        int [] newArray = new int[3];

        //多维数组MultiANewArrayInsnNode指令
        int [][] newArray2 = new int[5][6];



    }

    public static void main(String[] args) throws IOException {
        // 指定要读取的类的名称
        String className = ASMInstructionDemo.class.getName();
        ClassReader classReader = new ClassReader(className);

        // 创建 ClassNode 对象
        ClassNode classNode = new ClassNode();
        classReader.accept(classNode, 0);

        // 打印字节码
        printClassBytecode(classNode);
    }

    private static void printClassBytecode(ClassNode classNode) {
        System.out.println("Class: " + classNode.name);

        for (MethodNode method : classNode.methods) {
            if(!method.name.equals("testMethod")){
                continue;
            }

            Textifier textifier = new Textifier();
            TraceMethodVisitor tmv = new TraceMethodVisitor(textifier);

//            method.accept(tmv);   //函数整体输出
//            method.instructions.accept(tmv);  //指令输出

            Map<LabelNode,Integer> labelCount = new HashMap<>();

            for(AbstractInsnNode currentInsn = method.instructions.getFirst(); currentInsn != null; currentInsn = currentInsn.getNext()) {

                textifier.text.add("["+currentInsn.getClass().getSimpleName()+"] ");

                if (currentInsn instanceof InsnNode) {
                    InsnNode insn = (InsnNode) currentInsn;
                    textifier.text.add(Printer.OPCODES[insn.getOpcode()].toLowerCase());
                } else if (currentInsn instanceof FrameNode) {
                    FrameNode frame = (FrameNode) currentInsn;
                    textifier.text.add("FrameNode后面待定...");
                } else if (currentInsn instanceof LabelNode) {
                    LabelNode label = (LabelNode) currentInsn;
                    Integer labelIndex = labelCount.computeIfAbsent(label, key -> labelCount.size());
                    textifier.text.add("L"+labelIndex);
                } else if (currentInsn instanceof IntInsnNode) {
                    IntInsnNode intInsn = (IntInsnNode) currentInsn;
                    textifier.text.add(Printer.OPCODES[intInsn.getOpcode()].toLowerCase()+" "+intInsn.operand);
                } else if (currentInsn instanceof LdcInsnNode) {
                    LdcInsnNode ldcInsn = (LdcInsnNode) currentInsn;
                    textifier.text.add(Printer.OPCODES[ldcInsn.getOpcode()].toLowerCase()+" "+ldcInsn.cst);
                } else if (currentInsn instanceof VarInsnNode) {
                    VarInsnNode varInsn = (VarInsnNode) currentInsn;
                    textifier.text.add(Printer.OPCODES[varInsn.getOpcode()].toLowerCase()+" "+varInsn.var);
                } else if (currentInsn instanceof IincInsnNode) {
                    IincInsnNode iincInsn = (IincInsnNode) currentInsn;
                    textifier.text.add(Printer.OPCODES[iincInsn.getOpcode()].toLowerCase()+" "+iincInsn.var+","+iincInsn.incr);
                } else if (currentInsn instanceof JumpInsnNode) {
                    JumpInsnNode jumpInsn = (JumpInsnNode) currentInsn;
                    textifier.text.add(Printer.OPCODES[jumpInsn.getOpcode()].toLowerCase()+" "+jumpInsn.label);
                } else if (currentInsn instanceof TypeInsnNode) {
                    TypeInsnNode typeInsn = (TypeInsnNode) currentInsn;
                    textifier.text.add(Printer.OPCODES[typeInsn.getOpcode()].toLowerCase()+" "+typeInsn.desc);
                } else if (currentInsn instanceof FieldInsnNode) {
                    FieldInsnNode fieldInsn = (FieldInsnNode) currentInsn;
                    textifier.text.add(Printer.OPCODES[fieldInsn.getOpcode()].toLowerCase() +" "+ fieldInsn.owner+"."+fieldInsn.name+" "+fieldInsn.desc);
                } else if (currentInsn instanceof LineNumberNode) {
                    LineNumberNode lineNumberInsn = (LineNumberNode) currentInsn;
                    textifier.text.add("linenumber "+lineNumberInsn.line+" L"+labelCount.get(lineNumberInsn.start));
                } else if (currentInsn instanceof MethodInsnNode) {
                    MethodInsnNode methodInsn = (MethodInsnNode) currentInsn;
                    textifier.text.add(Printer.OPCODES[methodInsn.getOpcode()].toLowerCase()+" "+methodInsn.owner+"."+methodInsn.name+" "+methodInsn.desc);
                } else if (currentInsn instanceof TableSwitchInsnNode) {
                    TableSwitchInsnNode tableSwitchInsn = (TableSwitchInsnNode) currentInsn;
                    textifier.text.add("TableSwitchInsnNode后面待定...");
                } else if (currentInsn instanceof LookupSwitchInsnNode) {
                    LookupSwitchInsnNode lookupSwitchInsn = (LookupSwitchInsnNode) currentInsn;
                    textifier.text.add("LookupSwitchInsnNode后面待定...");
                } else if (currentInsn instanceof InvokeDynamicInsnNode) {
                    InvokeDynamicInsnNode invokeDynamicInsn = (InvokeDynamicInsnNode) currentInsn;
                    textifier.text.add("InvokeDynamicInsnNode后面待定...");
                } else if (currentInsn instanceof MultiANewArrayInsnNode) {
                    MultiANewArrayInsnNode multiANewArrayInsn = (MultiANewArrayInsnNode) currentInsn;
                    textifier.text.add(Printer.OPCODES[multiANewArrayInsn.getOpcode()].toLowerCase().toLowerCase()+" "+multiANewArrayInsn.desc+" "+multiANewArrayInsn.dims);
                }

                //上面自己写的输出 和 TraceMethodVisitor工具输出的对比
                //只有自己写输出，才能扩展输入，因为这只是一个逆向流程
                //TODO 后面跑一个自动化测试，多对比几个jar的所有class看自己写的和TraceMethodVisitor的效果是否一致
                textifier.text.add("    ==>");
                currentInsn.accept(tmv);
            }


            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            textifier.print(pw);
            pw.flush();

            System.out.println( sw.toString());
        }
    }
}
