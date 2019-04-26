package comp0012.main;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Stack;
import java.util.arrayList;

import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.Code;
import org.apache.bcel.classfile.JavaClass;
import org.apache.bcel.classfile.Method;
import org.apache.bcel.generic.*;
import org.apache.bcel.generic.ClassGen;
import org.apache.bcel.generic.ConstantPoolGen;
import org.apache.bcel.generic.InstructionHandle;
import org.apache.bcel.generic.InstructionList;
import org.apache.bcel.util.InstructionFinder;
import org.apache.bcel.generic.MethodGen;
import org.apache.bcel.generic.TargetLostException;



public class ConstantFolder
{
	ClassParser parser = null;
	ClassGen gen = null;

	JavaClass original = null;
	JavaClass optimized = null;

	Stack<Number> numberStack = null;

	public ConstantFolder(String classFilePath)
	{
		try{
			this.parser = new ClassParser(classFilePath);
			this.original = this.parser.parse();
			this.gen = new ClassGen(this.original);
		} catch(IOException e){
			e.printStackTrace();
		}
	}

	public void optimize(ClassGen cgen, ConstantPoolGen cpgen, Method method)
	{
		doConstantFolding(cgen, cpgen, method);
		doDynamicVariable(cgen, cpgen, method);
		doSimpleFolding(cgen, cpgen, method);
	}

	public void doConstantFolding(ClassGen cgen, ConstantPoolGen cpgen, Method method)
	{
		Code code = method.getCode();
		ArrayList<VariablesInfo> variables = new ArrayList<VariablesInfo>();
		InstructionList instructionList = new InstructionList(code.getCode());
		int count = 0;
		
		MethodGen methodGen = new MethodGen(method.getAccessFlags(), method.getReturnType(), method.getArgumentTypes(), null, method.getName(), cgen.getClassName(), instructionList, cpgen);

		for (InstructionHandle instructionHandle : instructionList.getInstructionHandles())
		{
			if (instructionHandle instanceof LocalVariableInstruction)
			{

			}
		}
	}

	public void doDynamicVariable(ClassGen cgen, ConstantPoolGen cpgen, Method method)
	{
		Code code = method.getCode();
		ArrayList<VariablesInfo> variables = new ArrayList<VariablesInfo>();
		InstructionList instructionList = new InstructionList(code.getCode());
		int count = 0;
		
		MethodGen methodGen = new MethodGen(method.getAccessFlags(), method.getReturnType(), method.getArgumentTypes(), null, method.getName(), cgen.getClassName(), instructionList, cpgen);
		for (InstructionHandle instructionHandle : instructionList.getInstructionHandles())
		{
			if (instructionHandle instanceof LocalVariableInstruction)
			{

			}
		}
	}

	private class VariablesInfo
	{
		String name;
		Number value;
		public VariablesInfo()
		{
			this.name = name;
			this.value = value;
		}
		public String getName() { return name; }
		public Number getValue() { return value; }
		public void setName(String name) { this.name = name; }
		public void setValue(Number value) { this.value = value; }
	}

	public void doSimpleFolding(ClassGen cgen, ConstantPoolGen cpgen, Method method)
	{
		Code code = method.getCode();
		numberStack = new Stack<Number>();
		InstructionList instructionList = new InstructionList(code.getCode());
		int count = 0;

		MethodGen methodGen = new MethodGen(method.getAccessFlags(), method.getReturnType(), method.getArgumentTypes(), null, method.getName(), cgen.getClassName(), instructionList, cpgen);

		for (InstructionHandle instructionHandle : instructionList.getInstructionHandles())
			if (iinstructionHandle.getInstruction() instanceof ArithmeticInstruction)
			{
				if (count > 1) deleteLDC(instructionHandle, instructionList, 1);
				else deleteLDC(instructionHandle, instructionList, 0);
				simpleFold(instructionHandle);
				Number first = numberStack.pop();
				count = count + 1;
				if (first instanceof Integer) instructionList.insert(instructionHandle, new LDC(cpgen.addInteger((Integer) first)));
				else if (first instanceof Long) instructionList.insert(instructionHandle, new LDC2_W(cpgen.addLong((Long) first)));
				else if (first instanceof Float) instructionList.insert(instructionHandle, new LDC(cpgen.addFloat((Float) first)));
				else if (first instanceof Double) instructionList.insert(instructionHandle, new LDC2_W(cpgen.addDouble((Double) first)));

				numberStack.push(first);
				deleteInstruction(instructionHandle, instructionList);
			}

		try
		{
			instructionList.setPositions(true);
		} catch (Exception e)
		{
			System.out.println("Set positions error");
		}

		methodGen.setMaxStack();
		methodGen.setMaxLocals();
		Method newMethod = methodGen.getMethod();
		cgen.replace(method, newMethod);
	}

	private void deleteLDC(InstructionHandle instructionHandle, InstructionList instructionList, int target){
		int methods = 0;
		InstructionHandle previousHandle = instructionHandle.getPrev();
		while (methods != target){
			if ((previousHandle.getInstruction() instanceof LDC) || previousHandle.getInstruction() instanceof LDC2_W) 
			{
				methods = methods + 1;
				if (methods < target) 
				{
					previousHandle = previousHandle.getPrev();
					deleteInstruction(previousHandle.getNext(), instructionList);
					continue;
				} 
				else deleteInstruction(previousHandle, instructionList);
			} 
			else if (previousHandle.getPrev() == null) break;
			previousHandle = previousHandle.getPrev();
		}
	}

	private void simpleFold(InstructionHandle instructionHandle){
		Number number1 = numberStack.pop();
		Number number2 = numberStack.pop();

		Instruction operation = instructionHandle.getInstruction();

		if (operation instanceof IADD)
		{
			Number result = number1.intValue() + number2.intValue();
			numberStack.push(result);			
		}
		else if (operation instanceof ISUB)
		{
			Number result = number1.intValue() - number2.intValue();
			numberStack.push(result);
		}
		else if (operation instanceof IMUL)
		{
			Number result = number1.intValue() * number2.intValue();
			numberStack.push(result);
		}
		else if (operation instanceof IDIV)
		{
			Number result = number1.intValue() / number2.intValue();
			numberStack.push(result);
		}
		else if (operation instanceof LADD)
		{
			Number result = number1.longValue() + number2.longValue();
			numberStack.push(result);
		}
		else if (operation instanceof LSUB)
		{
			Number result = number1.longValue() - number2.longValue();
			numberStack.push(result);
		}
		else if (operation instanceof LMUL)
		{
			Number result = number1.longValue() * number2.longValue();
			numberStack.push(result);
		}
		else if (operation instanceof LDIV)
		{
			Number result = number1.longValue() / number2.longValue();
			numberStack.push(result);
		}
		else if (operation instanceof FADD)
		{
			Number result = number1.floatValue() + number2.floatValue();
			numberStack.push(result);
		}
		else if (operation instanceof FSUB)
		{
			Number result = number1.floatValue() - number2.floatValue();
			numberStack.push(result);
		}
		else if (operation instanceof FMUL)
		{
			Number result = number1.floatValue() * number2.floatValue();
			numberStack.push(result);
		}
		else if (operation instanceof FDIV)
		{
			Number result = number1.floatValue() / number2.floatValue();
			numberStack.push(result);
		}
		else if (operation instanceof DADD)
		{
			Number result = number1.doubleValue() + number2.doubleValue();
			numberStack.push(result);
		}
		else if (operation instanceof DSUB)
		{
			Number result = number1.doubleValue() - number2.doubleValue();
			numberStack.push(result);
		}
		else if (operation instanceof DMUL)
		{
			Number result = number1.doubleValue() * number2.doubleValue();
			numberStack.push(result);
		}
		else if (operation instanceof DDIV)
		{
			Number result = number1.doubleValue() / number2.doubleValue();
			numberStack.push(result);
		} 
	}

	private void deleteInstruction(InstructionHandle instructionHandle, InstructionList instructionList){
		try 
		{
			instructionList.delete(instructionHandle);
		} catch (TargetLostException e)
		{
			for (InstructionHandle target : e.getTargets())
				for (InstructionTargeter targeter : target.getTargeters())
					targeter.updateTarget(target, null);
		}
	}
	
	public void optimize()
	{
		ClassGen cgen = new ClassGen(original);
		ConstantPoolGen cpgen = cgen.getConstantPool();

		Method[] methods = cgen.getMethods();
		for (Method method : methods) optimize(cgen, cpgen, method);
        
		this.optimized = gen.getJavaClass();
	}

	
	public void write(String optimisedFilePath)
	{
		this.optimize();

		try {
			FileOutputStream out = new FileOutputStream(new File(optimisedFilePath));
			this.optimized.dump(out);
		} catch (FileNotFoundException e) {
			// Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// Auto-generated catch block
			e.printStackTrace();
		}
	}
}