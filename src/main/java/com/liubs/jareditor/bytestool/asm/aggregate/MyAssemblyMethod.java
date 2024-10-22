package com.liubs.jareditor.bytestool.asm.aggregate;

import com.liubs.jareditor.bytestool.asm.entity.MyInstructionInfo;
import com.liubs.jareditor.bytestool.asm.entity.MyLineNumber;
import org.objectweb.asm.tree.*;
import org.objectweb.asm.util.Printer;

import java.util.*;

/**
 * @author Liubsyy
 * @date 2024/10/22
 */
public class MyAssemblyMethod {
    private MethodNode methodNode;

    public MyAssemblyMethod(MethodNode methodNode) {
        this.methodNode = methodNode;
    }

    public String name(){
        return methodNode.name;
    }

    private Map<LabelNode,Integer> buildLabelIndex(){
        ListIterator<?> instructions = methodNode.instructions.iterator();
        Map<LabelNode,Integer> labelIndexMap = new HashMap<>();
        while (instructions.hasNext()) {
            Object c = instructions.next();
            if(c instanceof LabelNode) {
                labelIndexMap.computeIfAbsent((LabelNode) c, key -> labelIndexMap.size());
            }
        }
        return labelIndexMap;
    }

    public MethodNode getMethodNode() {
        return methodNode;
    }

    public MyInstructionInfo buildInstructionInfo(){
        int lineCounter = 0;
        StringBuilder assmblyBuild = new StringBuilder();
        List<MyLineNumber> markLines = new ArrayList<>();
        List<FrameNode> frameNodes = new ArrayList<>();

        /** 先遍历LabelNode构建索引编号*/
        Map<LabelNode,Integer> labelIndexMap = buildLabelIndex();

        /**
         * 遍历字节码指令信息，包括指令文本，行号信息，FrameNode信息
         * */
        ListIterator<?> instructions = methodNode.instructions.iterator();
        while (instructions.hasNext()) {
            Object currentInsn = instructions.next();

            if (currentInsn instanceof InsnNode) {
                InsnNode insn = (InsnNode) currentInsn;
                assmblyBuild.append(Printer.OPCODES[insn.getOpcode()].toLowerCase());
            } else if (currentInsn instanceof FrameNode) {
                frameNodes.add((FrameNode)currentInsn );
                continue;
            } else if (currentInsn instanceof LabelNode) {
                if(!instructions.hasNext()){
                    continue;
                }
                LabelNode label = (LabelNode) currentInsn;
                MyLineNumber lineNumber = new MyLineNumber();
                lineNumber.setLineEditor(lineCounter);
                lineNumber.setLabelIndex(labelIndexMap.get(label));
                markLines.add(lineNumber);
                continue;
            } else if (currentInsn instanceof IntInsnNode) {
                IntInsnNode intInsn = (IntInsnNode) currentInsn;
                assmblyBuild.append(Printer.OPCODES[intInsn.getOpcode()].toLowerCase()+" "+intInsn.operand);
            } else if (currentInsn instanceof LdcInsnNode) {
                LdcInsnNode ldcInsn = (LdcInsnNode) currentInsn;
                assmblyBuild.append(Printer.OPCODES[ldcInsn.getOpcode()].toLowerCase()+" "+ldcInsn.cst);
            } else if (currentInsn instanceof VarInsnNode) {
                VarInsnNode varInsn = (VarInsnNode) currentInsn;
                assmblyBuild.append(Printer.OPCODES[varInsn.getOpcode()].toLowerCase()+" "+varInsn.var);
            } else if (currentInsn instanceof IincInsnNode) {
                IincInsnNode iincInsn = (IincInsnNode) currentInsn;
                assmblyBuild.append(Printer.OPCODES[iincInsn.getOpcode()].toLowerCase()+" "+iincInsn.var+","+iincInsn.incr);
            } else if (currentInsn instanceof JumpInsnNode) {
                JumpInsnNode jumpInsn = (JumpInsnNode) currentInsn;
                Integer index = labelIndexMap.get(jumpInsn.label);
                assmblyBuild.append(Printer.OPCODES[jumpInsn.getOpcode()].toLowerCase()+" L"+index);
            } else if (currentInsn instanceof TypeInsnNode) {
                TypeInsnNode typeInsn = (TypeInsnNode) currentInsn;
                assmblyBuild.append(Printer.OPCODES[typeInsn.getOpcode()].toLowerCase()+" "+typeInsn.desc);
            } else if (currentInsn instanceof FieldInsnNode) {
                FieldInsnNode fieldInsn = (FieldInsnNode) currentInsn;
                assmblyBuild.append(Printer.OPCODES[fieldInsn.getOpcode()].toLowerCase() +" "+ fieldInsn.owner+"."+fieldInsn.name+" "+fieldInsn.desc);
            } else if (currentInsn instanceof LineNumberNode) {
                LineNumberNode lineNumberInsn = (LineNumberNode) currentInsn;
                for(int i = markLines.size()-1 ; i>=0 ;i--){
                    MyLineNumber lineNumber = markLines.get(i);
                    if(lineNumber.getLabelIndex() == labelIndexMap.get(lineNumberInsn.start)) {
                        lineNumber.setLineSource(lineNumberInsn.line);
                        break;
                    }
                }
                continue;
            } else if (currentInsn instanceof MethodInsnNode) {
                MethodInsnNode methodInsn = (MethodInsnNode) currentInsn;
                assmblyBuild.append(Printer.OPCODES[methodInsn.getOpcode()].toLowerCase()).append(" ").append(methodInsn.owner).append(".").append(methodInsn.name).append(" ").append(methodInsn.desc);
            } else if (currentInsn instanceof TableSwitchInsnNode) {
                TableSwitchInsnNode tableSwitchInsn = (TableSwitchInsnNode) currentInsn;
                assmblyBuild.append(Printer.OPCODES[tableSwitchInsn.getOpcode()].toLowerCase()).append(" ").append(tableSwitchInsn.min).append(" to ").append(tableSwitchInsn.max).append("\n");
                lineCounter++;
                for(int i = 0,len=tableSwitchInsn.labels.size(); i < len; i++) {
                    assmblyBuild.append("          ").append(tableSwitchInsn.min + i).append(": ");
                    assmblyBuild.append("L").append(labelIndexMap.get(tableSwitchInsn.labels.get(i)));
                    assmblyBuild.append('\n');
                    lineCounter++;
                }
                assmblyBuild.append("          default: L").append(labelIndexMap.get(tableSwitchInsn.dflt));

            } else if (currentInsn instanceof LookupSwitchInsnNode) {
                LookupSwitchInsnNode lookupSwitchInsn = (LookupSwitchInsnNode) currentInsn;
                assmblyBuild.append(Printer.OPCODES[lookupSwitchInsn.getOpcode()].toLowerCase()).append("\n");
                lineCounter++;
                for(int i = 0,len=lookupSwitchInsn.labels.size(); i < len; i++) {
                    assmblyBuild.append("          ").append(lookupSwitchInsn.keys.get(i)).append(": ");
                    assmblyBuild.append("L").append(labelIndexMap.get(lookupSwitchInsn.labels.get(i)));
                    assmblyBuild.append('\n');
                    lineCounter++;
                }
                assmblyBuild.append("          default: L").append(labelIndexMap.get(lookupSwitchInsn.dflt));

            } else if (currentInsn instanceof InvokeDynamicInsnNode) {
                InvokeDynamicInsnNode invokeDynamicInsn = (InvokeDynamicInsnNode) currentInsn;
                assmblyBuild.append(Printer.OPCODES[invokeDynamicInsn.getOpcode()].toLowerCase().toLowerCase()).append(" ")
                        .append(invokeDynamicInsn.name).append(" ").append(invokeDynamicInsn.desc)
                        .append(" handle{").append(invokeDynamicInsn.bsm.getOwner()).append(".").append(invokeDynamicInsn.bsm.getName()).append(" ").append(invokeDynamicInsn.bsm.getDesc()).append("}")
                        .append(" args{").append(Arrays.toString(invokeDynamicInsn.bsmArgs)).append("}");
            } else if (currentInsn instanceof MultiANewArrayInsnNode) {
                MultiANewArrayInsnNode multiANewArrayInsn = (MultiANewArrayInsnNode) currentInsn;
                assmblyBuild.append(Printer.OPCODES[multiANewArrayInsn.getOpcode()].toLowerCase().toLowerCase()).append(" ").append(multiANewArrayInsn.desc).append(" ").append(multiANewArrayInsn.dims);
            }

            assmblyBuild.append("\n");
            lineCounter++;
        }


        MyInstructionInfo myInstructionInfo = new MyInstructionInfo();
        myInstructionInfo.setAssemblyCode(assmblyBuild.toString());
        myInstructionInfo.setMarkLines(markLines);
        myInstructionInfo.setFrameNodes(frameNodes);
        myInstructionInfo.setLabelIndexMap(labelIndexMap);

        return myInstructionInfo;
    }
}
