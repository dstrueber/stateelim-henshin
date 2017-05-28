package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

import transitiongraph.TransitionGraph;
import transitiongraph.TransitiongraphPackage;

public abstract class TestFramework {
	final Duration TIMEOUT_DURATION = Duration.ofMinutes(60);

	public static String pathToMainModels = "testdata/emf/task-main";
	public static String pathToExtension1Models = "testdata/emf/task-extension1";
	public static String pathToExtension2Models = "testdata/emf/task-extension2";
	public static String pathToPositive = "testdata/acceptedWords";
	public static String pathToNegative = "testdata/notAcceptedWords";

	public abstract String FSAToRegex(TransitionGraph fsa);

	public abstract String DTMCToSRE(TransitionGraph dtmc);
	
	public abstract TransitionGraph simplifyFSA(TransitionGraph tg);

	public void testFSAToRegexAllModels() throws FileNotFoundException {

		File[] modelFiles = new File(pathToMainModels).listFiles();
		String result;
		StringBuilder resultData = new StringBuilder();
		String transformationType = "fsa2regex";

		resultData.append("modelname,");
		resultData.append("size of the regular expression,");
		resultData.append("time to transform (ms),");
		resultData.append("correctly accepted words,");
		resultData.append("correctly not accepted words");
		resultData.append("\n");

		PrintWriter pw = new PrintWriter(new File("testresult/result_fsa2regex.txt"));
		pw.write(resultData.toString());
		pw.close();

		for (File modelFile : modelFiles) {
			result = testModelToRegex(modelFile.getName(), transformationType);
			pw = new PrintWriter(new FileOutputStream(new File("testresult/result_fsa2regex.txt"), true));
			pw.write(result);
			pw.close();
		}

		System.out.println("done!");
	}

	public String testModelToRegex(String modelFileName, String transformationType) throws FileNotFoundException {
		String modelName;
		String regex = "";
		int regexSize;
		String acceptedWordsFile, notAcceptedWordsFile;
		StringBuilder resultData = new StringBuilder();
		TransitionGraph tg = null;
		long timeToTransform;
	
		if (!modelFileName.matches(".+\\.xmi"))
			return "";
	
		modelName = modelFileName.replaceAll("\\.xmi", "");
		
		if (transformationType.equals("fsa2regex"))
			tg = getTransitionGraphFromXMI(modelName, pathToMainModels);
		 else if(transformationType.equals("dtmc2sre"))
			tg = getTransitionGraphFromXMI(modelName, pathToExtension2Models);
	
		timeToTransform = System.currentTimeMillis();
		regex = convertWithTimeout(tg, transformationType);
		timeToTransform = System.currentTimeMillis() - timeToTransform;
	
		if (Character.isDigit(modelName.charAt(0)))
			modelName = modelName.substring(3);
		resultData.append(modelName + ",");
		if (regex.equals("timeout")) {
			resultData.append("Timeout");
		} else {
	
			regexSize = getRegexSize(regex);
	
			acceptedWordsFile = pathToPositive + "/" + modelName + "-positive.data";
			notAcceptedWordsFile = pathToNegative + "/" + modelName + "-negative.data";
	
			resultData.append(regexSize + ",");
			resultData.append(timeToTransform + ",");
			resultData.append(testWords(regex, acceptedWordsFile, true) + ",");
			resultData.append(testWords(regex, notAcceptedWordsFile, false));
		}
		resultData.append("\n");
	
		System.out.println(modelName + " done.");
		return resultData.toString();
	}

	private String convertWithTimeout(TransitionGraph tg, String transformationType) {
		ExecutorService executor = Executors.newSingleThreadExecutor();
		Future<String> future = executor.submit(new ConversionTask(tg, transformationType));
		String result = "";
		try {
			result = future.get(TIMEOUT_DURATION.toMillis(), TimeUnit.MILLISECONDS);
		} catch (TimeoutException | InterruptedException | ExecutionException e) {
			result = "timeout";
		}
		executor.shutdownNow();
		return result;
	}

	public void testSimplifyFSAAllModels() throws FileNotFoundException {
		File[] modelFiles = new File(pathToExtension1Models).listFiles();
		String result;
		StringBuilder resultData = new StringBuilder();

		resultData.append("modelname,");
		resultData.append("result correct?,");
		resultData.append("\n");

		PrintWriter pw = new PrintWriter(new File("testresult/result_simplifyrsa.txt"));
		pw.write(resultData.toString());
		pw.close();

		for (File modelFile : modelFiles) {
			 result = testSimplifyFSA(modelFile.getName());
			pw = new PrintWriter(new FileOutputStream(new File("testresult/result_simplifyrsa.txt"), true));
			pw.write(result);
			pw.close();
		}

		System.out.println("done!");
	}

	public String testSimplifyFSA(String modelFileName) throws FileNotFoundException {
		String modelName;
		TransitionGraph tg;
		long timeToTransform;
	
	
		if (!modelFileName.matches(".+\\.xmi"))
			return "";
	
		modelName = modelFileName.replaceAll("\\.xmi", "");
		tg = getTransitionGraphFromXMI(modelName, pathToExtension1Models);
		TransitionGraph originalTg = EcoreUtil.copy(tg);
		
		timeToTransform = System.currentTimeMillis();
		tg = simplifyFSA(tg);
		timeToTransform = System.currentTimeMillis() - timeToTransform;
	
		boolean correct = simplificationIsCorrect(originalTg, tg);
		
		StringBuilder resultData = new StringBuilder();
		resultData.append(modelName + ",");
		resultData.append(timeToTransform + ",");
		resultData.append(correct + "\n");
		
		System.out.println(modelName + " done.");
		return resultData.toString();
	}

	private boolean simplificationIsCorrect(TransitionGraph oldTg, TransitionGraph newTg) {
		boolean result = false;
		int oldStates = oldTg.getStates().size();
		int oldTransitions = oldTg.getTransitions().size();
		int oldInitial = oldTg.getStates().stream().filter(s->s.isIsInitial()).collect(Collectors.toSet()).size();
		int oldFinal = oldTg.getStates().stream().filter(s->s.isIsFinal()).collect(Collectors.toSet()).size();
	
		int newStates = newTg.getStates().size();
		int newTransitions = newTg.getTransitions().size();
		int newInitial = newTg.getStates().stream().filter(s->s.isIsInitial()).collect(Collectors.toSet()).size();
		int newFinal = newTg.getStates().stream().filter(s->s.isIsFinal()).collect(Collectors.toSet()).size();
		
		if (oldInitial == 1 && oldFinal == 1) {
			result = (oldStates == newStates) && (oldTransitions == newTransitions) && (oldInitial == newInitial) && (oldFinal == newFinal);
		} else if (oldInitial > 1 && oldFinal == 1) {
			int delta = oldInitial;
			result = (newStates == oldStates+1) && (newTransitions == oldTransitions+delta) && (newInitial == 1) && (oldFinal == newFinal);
		} else if (oldInitial == 1 && oldFinal > 1) {
			int delta = oldFinal;
			result = (newStates == oldStates+1) && (newTransitions == oldTransitions+delta) && (newInitial == oldInitial) && (newFinal == 1);
		} else if (oldInitial > 1 && oldFinal > 1) {
			int delta = oldInitial+oldFinal;
			result = (newStates == oldStates + 2) && (newTransitions == oldTransitions+delta) && (newInitial == 1) && (newFinal == 1);
		}
		return result;
	}

	public void testDTMCToSREAllModels() throws FileNotFoundException {

		PrintWriter pw = new PrintWriter(new File("testresult/result_dtmc2sre.txt"));
		File[] modelFiles = new File(pathToExtension2Models).listFiles();
		String result;
		StringBuilder resultData = new StringBuilder();
		String transformationType = "dtmc2sre";

		resultData.append("modelname,");
		resultData.append("size of the regular expression,");
		resultData.append("time to transform (ms),");
		resultData.append("correctly accepted words,");
		resultData.append("correctly not accepted words");
		resultData.append("\n");

		for (File modelFile : modelFiles) {
			result = testModelToRegex(modelFile.getName(), transformationType);
			resultData.append(result);
		}

		pw.write(resultData.toString());
		pw.close();
		System.out.println("done!");
	}

	public TransitionGraph getTransitionGraphFromXMI(String modelName, String path) {

		TransitiongraphPackage.eINSTANCE.eClass();

		Resource.Factory.Registry reg = Resource.Factory.Registry.INSTANCE;
		Map<String, Object> m = reg.getExtensionToFactoryMap();
		m.put("xmi", new XMIResourceFactoryImpl());

		ResourceSet resSet = new ResourceSetImpl();

		Resource resource = resSet.getResource(URI.createURI(path + "/" + modelName + ".xmi"), true);

		TransitionGraph tg = (TransitionGraph) resource.getContents().get(0);

		return tg;
	}

	private int getRegexSize(String regex) {
		int size = 0;

		for (int i = 0; i < regex.length(); i++) {
			if (regex.charAt(i) == 's')
				size++;
		}

		return size;
	}

	private String formatRegex(String regex) {
		regex = regex.replace('+', '|'); // java uses '|' as the or symbol
		regex = regex.replaceAll("\\[.*?\\]", ""); // remove probability
		regex = regex.replaceAll(":", ""); // ':' is concatenation
		return regex;
	}

	private String testWords(String regex, String acceptedWordsFile, boolean accept) {
		int totalWords = 0;
		int passed = 0;
		String word = "";
		BufferedReader reader;

		regex = formatRegex(regex);
		try {
			reader = new BufferedReader(new FileReader(acceptedWordsFile));
			while ((word = reader.readLine()) != null) {
				totalWords++;
				if (word.matches(regex) == accept)
					passed++;
				else
					System.err.println(word + " " + regex);

			}
			reader.close();
		} catch (Exception e) {

		}
		return passed + "/" + totalWords;
	}

	class ConversionTask implements Callable<String> {
		TransitionGraph tg;
		String transformationType;

		public ConversionTask(TransitionGraph tg, String task) {
			this.tg = tg;
			this.transformationType = task;
		}

		@Override
		public String call() throws Exception {
			if (transformationType.equals("fsa2regex"))
				return FSAToRegex(tg);
			if (transformationType.equals("dtmc2sre"))
				return DTMCToSRE(tg);
			throw new RuntimeException("Invalid transformation type: "+transformationType);
		}

	}
}