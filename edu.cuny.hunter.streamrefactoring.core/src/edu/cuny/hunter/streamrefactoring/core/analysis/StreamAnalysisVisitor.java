package edu.cuny.hunter.streamrefactoring.core.analysis;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility2;
import org.eclipse.jdt.internal.corext.dom.Bindings;
import org.eclipse.jdt.internal.corext.refactoring.util.JavaElementUtil;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;

import edu.cuny.hunter.streamrefactoring.core.descriptors.ConvertStreamToParallelRefactoringDescriptor;
import edu.cuny.hunter.streamrefactoring.core.utils.Util;

public class StreamAnalysisVisitor extends ASTVisitor {
	private Set<Stream> streamSet = new HashSet<>();

	public StreamAnalysisVisitor() {
		super();
	}

	public StreamAnalysisVisitor(boolean visitDocTags) {
		super(visitDocTags);
	}

	@SuppressWarnings("restriction")
	@Override
	public boolean visit(MethodInvocation node) {
		IMethodBinding methodBinding = node.resolveMethodBinding();
		ITypeBinding returnType = methodBinding.getReturnType();
		boolean returnTypeImplementsBaseStream = implementsBaseStream(returnType);
		
		ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		boolean declaringClassImplementsBaseStream = implementsBaseStream(declaringClass);

		// java.util.stream.BaseStream is the top-level interface for all
		// streams. Make sure we don't include intermediate operations.
		if (returnTypeImplementsBaseStream && !declaringClassImplementsBaseStream) {
			Stream stream = new Stream(node);
			inferStreamExecution(stream, methodBinding);

			System.out.println(stream);
		}

		return super.visit(node);
	}

	private void inferStreamExecution(Stream stream, IMethodBinding methodBinding) {
		IMethod method = (IMethod) methodBinding.getJavaElement();

		String methodIdentifier = null;
		try {
			methodIdentifier = Util.getMethodIdentifier(method);
		} catch (JavaModelException e) {
			throw new RuntimeException(e);
		}

		if (methodIdentifier.equals("parallelStream()"))
			stream.setExecutionMode(StreamExecutionMode.PARALLEL);
		else
			stream.setExecutionMode(StreamExecutionMode.SEQUENTIAL);
	}

	private static boolean implementsBaseStream(ITypeBinding type) {
		Set<ITypeBinding> implementedInterfaces = getImplementedInterfaces(type);
		return implementedInterfaces.stream()
				.anyMatch(i -> i.getErasure().getQualifiedName().equals("java.util.stream.BaseStream"));
	}

	private static Set<ITypeBinding> getImplementedInterfaces(ITypeBinding type) {
		Set<ITypeBinding> ret = new HashSet<>();

		if (type.isInterface())
			ret.add(type);

		ret.addAll(getAllInterfaces(type));
		return ret;
	}

	private static Set<ITypeBinding> getAllInterfaces(ITypeBinding type) {
		Set<ITypeBinding> ret = new HashSet<>();
		ITypeBinding[] interfaces = type.getInterfaces();
		ret.addAll(Arrays.asList(interfaces));

		for (ITypeBinding interfaceBinding : interfaces)
			ret.addAll(getAllInterfaces(interfaceBinding));

		return ret;
	}

	public Set<Stream> getStreamSet() {
		return streamSet;
	}

}
