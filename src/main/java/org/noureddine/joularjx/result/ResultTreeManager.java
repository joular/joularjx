package org.noureddine.joularjx.result;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.noureddine.joularjx.utils.AgentProperties;
import org.noureddine.joularjx.utils.JoularJXLogging;

public class ResultTreeManager {

    private static final Logger logger = JoularJXLogging.getLogger();

    // Folders names
    public final static String GLOBAL_RESULT_DIRECTORY_NAME = "joularjx-result";

    public final static String ALL_DIRECTORY_NAME = "all";
    public final static String FILTERED_DIRECTORY_NAME = "app";

    public final static String RUNTIME_DIRECTORY_NAME = "runtime";
    public final static String TOTAL_DIRECTORY_NAME = "total";
    public final static String EVOLUTION_DIRECTORY_NAME = "evolution";

    public final static String CALLTREE_DIRECTORY_NAME = "calltrees";
    public final static String METHOD_DIRECTORY_NAME = "methods";

    private AgentProperties properties;

    //The directory where the result of the current execution will be written
    private String runDirectoryPath;

    //All leaf directory paths
    private String allTotalMethodsPath;
    private String filteredTotalMethodsPath;
    private String allRuntimeMethodsPath;
    private String filteredRuntimeMethodsPath;

    private String allRuntimeCallTreePath;
    private String filteredRuntimeCallTreePath;
    private String allTotalCallTreePath;
    private String filteredTotalCallTreePath;

    private String allEvolutionPath;
    private String filteredEvolutionPath;

    public ResultTreeManager(AgentProperties properties, String appName, long pid, long startTimestamp) {
        this.properties = properties;
        
        //Building the path of all the directories
        this.runDirectoryPath =  GLOBAL_RESULT_DIRECTORY_NAME + "/" + String.format("%s-%d-%d",appName, pid, startTimestamp);

        String allDirectoryPath      = runDirectoryPath + "/" + ALL_DIRECTORY_NAME;
        String filteredDirectoryPath = runDirectoryPath + "/" + FILTERED_DIRECTORY_NAME;

        
        this.allTotalMethodsPath        = allDirectoryPath + "/" + TOTAL_DIRECTORY_NAME + "/" + METHOD_DIRECTORY_NAME;
        this.filteredTotalMethodsPath   = filteredDirectoryPath + "/" + TOTAL_DIRECTORY_NAME + "/" + METHOD_DIRECTORY_NAME;
        this.allRuntimeMethodsPath      = allDirectoryPath + "/" + RUNTIME_DIRECTORY_NAME + "/" + METHOD_DIRECTORY_NAME;
        this.filteredRuntimeMethodsPath = filteredDirectoryPath + "/" + RUNTIME_DIRECTORY_NAME + "/" + METHOD_DIRECTORY_NAME;

        this.allRuntimeCallTreePath      = allDirectoryPath + "/" + RUNTIME_DIRECTORY_NAME + "/" + CALLTREE_DIRECTORY_NAME;
        this.filteredRuntimeCallTreePath = filteredDirectoryPath + "/" + RUNTIME_DIRECTORY_NAME + "/" + CALLTREE_DIRECTORY_NAME;
        this.allTotalCallTreePath        = allDirectoryPath + "/" + TOTAL_DIRECTORY_NAME + "/" + CALLTREE_DIRECTORY_NAME;
        this.filteredTotalCallTreePath   = filteredDirectoryPath + "/" + TOTAL_DIRECTORY_NAME + "/" + CALLTREE_DIRECTORY_NAME;

        this.allEvolutionPath      = allDirectoryPath + "/" + EVOLUTION_DIRECTORY_NAME;
        this.filteredEvolutionPath = filteredDirectoryPath + "/" + EVOLUTION_DIRECTORY_NAME;

    }

    public boolean create() {
        //This boolean acts as a check. If an error occurs during the intialization of the file hierarchy, the method will continue its execution, as other directories may be created sucessfully, but this boolean will be set to false, to indicate that an error occured.
        boolean verif = true; 

        logger.log(Level.INFO, String.format("Results will be stored in %s/", this.runDirectoryPath));

        //List of all the directories that will be created
        List<String> directoriesToCreate = new ArrayList<>();

        //Mandatory directories (directories that do not depend of configuration properties)
        directoriesToCreate.add(this.allTotalMethodsPath);
        directoriesToCreate.add(this.filteredTotalMethodsPath);

        //Optional directories (directories that depends of configuration properties)
        //Runtime
        if (properties.savesRuntimeData()) {
            directoriesToCreate.add(this.allRuntimeMethodsPath);
            directoriesToCreate.add(this.filteredRuntimeMethodsPath);
        }

        //Call trees
        if (properties.callTreesConsumption()) {
            //Runtime
            if (properties.saveCallTreesRuntimeData()) {
                directoriesToCreate.add(this.allRuntimeCallTreePath);
                directoriesToCreate.add(this.filteredRuntimeCallTreePath);
            }
            //Total
            directoriesToCreate.add(this.allTotalCallTreePath);
            directoriesToCreate.add(this.filteredTotalCallTreePath);
        }

        //Methods consumption evolution
        if (properties.trackConsumptionEvolution()) {
            directoriesToCreate.add(this.allEvolutionPath);
            directoriesToCreate.add(this.filteredEvolutionPath);
        }

        //Creating all the directories
        for (String dirPath : directoriesToCreate) {
            File dir = new File(dirPath);
            if (!dir.exists() && !dir.mkdirs()) {
                logger.log(Level.WARNING, String.format("Failed to create directory %s", dirPath));
                verif = false;
            }
        }

        return verif;
    }
    
}
