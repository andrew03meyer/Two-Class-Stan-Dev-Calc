import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;

public class SDCalc{
    public static void main(String[] args){
        Process();
    }

    public static void Process(){
        Double trd[][] = GetTrainingData();
        Double tsd[][] = GetTestingData();
        //int[] trl = GetTrainingLabel();
        int[] tsl = GetTestLabel();
        int[] out = GetOutputLabel();
        String classes = "";

        for(int x = 0; x < 200; x++){
            classes += " " + String.valueOf(EuclideanCompare(tsd[x], trd));
        }
        WriteClassData(classes);
        System.out.println(CompareLabels(out, tsl));
        ColSDReduction(trd);
    }

    public static Double[][] GetTrainingData(){
        Double[][] trainingData = new Double[200][61];

        try{
            File trainingFile = new File("train_data.txt");
            Scanner tfScanner = new Scanner(trainingFile);
            
            int rowIndex = 0;
            int columnIndex;

            while(tfScanner.hasNextLine()){    
                columnIndex = 0;
                //Split line by space and insert into the 2D array
                String[] temp = tfScanner.nextLine().split(" ");
                for(String item : temp){
                    trainingData[rowIndex][columnIndex] = Double.parseDouble(item);
                    columnIndex++;
                }
                rowIndex++;
            }
            tfScanner.close();
        }
        catch(Exception e){
            System.out.println("Exception: " + e);
        }
        return trainingData;
    }

    public static Double[][] GetTestingData(){
        Double[][] testingData = new Double[200][61];

        try{
            File testingFile = new File("test_data.txt");
            Scanner tsfScanner = new Scanner(testingFile);
            
            int rowIndex = 0;
            int columnIndex;

            while(tsfScanner.hasNextLine()){    
                columnIndex = 0;
                //Split line by space and insert into the 2D array
                String[] temp = tsfScanner.nextLine().split(" ");
                for(String item : temp){
                    testingData[rowIndex][columnIndex] = Double.parseDouble(item);
                    columnIndex++;
                }
                rowIndex++;
            }
            tsfScanner.close();
        }
        catch(Exception e){
            System.out.println("Exception: " + e);
        }
        return testingData;
    }

    public static int[] GetTrainingLabel(){
        try{
            File trainingLabelFile = new File("train_label.txt");
            Scanner tlScanner = new Scanner(trainingLabelFile);

            String[] temp = tlScanner.nextLine().stripLeading().split(" ");
            int index = 0;
            int[] trainingLabels = new int[200];

            for(String items:temp){
                trainingLabels[index] = Integer.parseInt(items);
                index++;
            }
            tlScanner.close();
            return trainingLabels;
        }
        catch(Exception e){
            System.out.println("Exception: " + e);
            int[] temp = {};
            return temp;
        }
    }

    public static int[] GetTestLabel(){
        try{
            File testLabelFile = new File("test_label.txt");
            Scanner tslScanner = new Scanner(testLabelFile);

            String[] temp = tslScanner.nextLine().stripLeading().split(" ");
            int index = 0;
            int[] testLabels = new int[200];

            for(String items:temp){
                testLabels[index] = Integer.parseInt(items);
                index++;
            }
            tslScanner.close();
            return testLabels;
        }
        catch(Exception e){
            System.out.println("Exception: " + e);
            int[] temp = {};
            return temp;
        }
    }

    public static int[] GetOutputLabel(){
        try{
            File outLabelFile = new File("output2.txt");
            Scanner outScanner = new Scanner(outLabelFile);

            String[] temp = outScanner.nextLine().stripLeading().split(" ");
            int index = 0;
            int[] outLabels = new int[200];

            for(String items:temp){
                outLabels[index] = Integer.parseInt(items);
                index++;
            }
            outScanner.close();
            return outLabels;
        }
        catch(Exception e){
            System.out.println("Exception: " + e);
            int[] temp = {};
            return temp;
        }
    }

    /*
     * Work out the difference of one test row to every other row in training data
     */
    public static int EuclideanCompare(Double[] testingData, Double[][] trainingData){
        Double[] differences = new Double[200];
        for(int row = 0; row < 200; row++){
            differences[row] = Double.parseDouble("0");
            for(int column = 0; column < 61; column++){
                differences[row] = differences[row] + (Math.abs((trainingData[row][column]) - (testingData[column]) * Math.abs((trainingData[row][column]) - (testingData[column]))));
            }
            differences[row] = Math.sqrt(differences[row]);
        }
        int index = getClass(differences);
        return index;
    }

    /*
     * find the class of item based on kNN (k=5)
     * Parameters - Array of Euclidean distances of each row
     * Return 0/1 based on 5 closest values
     */
    public static int getClass(Double[] euclDist){

        //Create a HashMap to negate loss of index
        HashMap<Double, Integer> hash1 = new HashMap<Double, Integer>();
        for(int y = 0; y<200; y++){
            hash1.put(euclDist[y], y);
        }

        //Sort the array
        Arrays.sort(euclDist);

        //Variables for non/alchoholic count
        int alc = 0;
        int non = 0;

        //Take the top 5 items
        for(int x = 0; x < 5; x++){
            //Find the index of the item
            int index = hash1.get(euclDist[x]);
            //If the training label of that index is 0, increment non
            if(GetTrainingLabel()[index] == 0){
                non++;
            }
            //Otherwise, increment alc
            else{
                alc++;
            }
        }
        //Return which class, based on alc & non counts
        if(non > alc){return 0;}else{return 1;}
    }

    /*
     * Write the String param to a file (output2.txt)
     */
    public static void WriteClassData(String itemClass){
        try{
            File classData = new File("output2.txt");
                FileWriter fwr = new FileWriter(classData);
                fwr.write(itemClass);
                fwr.close();
        }
        catch(Exception e){
            System.out.println("File already exists");
        }
    }

    /*
     * Parameters - knn2 output, real classifications
     * Returns percentage accuracy
     */
    public static Double CompareLabels(int[] out, int[] tsl){
        int y=0;
        for(int x = 0; x < 200; x++){
            if(out[x] == tsl[x]){
                y++;
            }
        }
        System.out.println(y);
        Double temp = (Double.parseDouble(String.valueOf(y)))*(Double.parseDouble("100"))/(Double.parseDouble("200"));
        return temp;
    }

  /*
     * Works out the standard deviation of each column and 
     * then removes data from more than xSD from the mean
     */
    public static void ColSDReduction(Double[][] trData){
        int[] trLabels = GetTrainingLabel();
        int alcoInt = 0;
        int non_alcoInt = 0;

        //Set the array sizes
        for(int x = 0; x < 200; x++){
            if(trLabels[x] == 1){alcoInt++;}
            else{non_alcoInt++;}
        }

        Double[] SDColumnsAlco = new Double[alcoInt];
        Double[] SDColumnsNon = new Double[non_alcoInt];

        Double[] alcoMean = new Double[alcoInt];
        Double[] nonAlcoMean = new Double[non_alcoInt];

        //Take each columns SD and store in seperate arrays for Alcoholic and Non-alcoholic
        for(int col=0; col < 61; col++){

            SDColumnsNon[col] = Double.parseDouble("0");
            SDColumnsAlco[col] = Double.parseDouble("0");
            nonAlcoMean[col] = Double.parseDouble("0");
            alcoMean[col] = Double.parseDouble("0");

            //Calculate mean per column
            for(int row = 0; row < 200; row++){
                if(trLabels[row] == 0){
                    nonAlcoMean[col] += trData[row][col];
                }
                else{
                    alcoMean[col] += trData[row][col];
                }
            }

            //System.out.println("Sum Non: " + nonAlcoMean[col] + "       Sum Alco: " + alcoMean[col]);

            //Sum rows / num rows (per column)
            nonAlcoMean[col] /= non_alcoInt;
            alcoMean[col] /= alcoInt;

            //System.out.println("Mean Non: " + nonAlcoMean[col] + "       Mean Alco: " + alcoMean[col]);

            //Calculate SD per column
            for(int row = 0; row < 200; row++){
                if(trLabels[row] == 0){
                    SDColumnsNon[col] += Math.pow((trData[row][col] - nonAlcoMean[col]), 2);
                }
                else{
                    SDColumnsAlco[col] += Math.pow((trData[row][col] - alcoMean[col]), 2);
                }
            }

            SDColumnsNon[col] = Math.sqrt(SDColumnsNon[col] / (non_alcoInt-1));
            SDColumnsAlco[col] = Math.sqrt(SDColumnsAlco[col] / (alcoInt-1));
            
            System.out.println("Column: " + col + "     SD Non: " + SDColumnsNon[col] + "       SD Alco: " + SDColumnsAlco[col]);
        }

        //Delete rows by adding valid ones to the arrayList
        //ArrayList<ArrayList> grid = new ArrayList();

        int issues = 0;

        for(int col=0; col < 61; col++){
            
            Double upperboundNon = nonAlcoMean[col] + SDColumnsNon[col];
            Double lowerboundNon = nonAlcoMean[col] - SDColumnsNon[col];
            
            Double upperboundAlco = alcoMean[col] + SDColumnsAlco[col];
            Double lowerboundAlco = alcoMean[col] - SDColumnsAlco[col];
            System.out.println("¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬");
            System.out.println("| Upperbound Non: " + upperboundNon + "       Lowerbound Non: " + lowerboundNon + "|");
            System.out.println("| Upperbound Alco: " + upperboundAlco + "       Lowerbound Alco: " + lowerboundAlco + "|");
            System.out.println("¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬¬");

            for(int row=0; row < 200; row++){
                //System.out.println(trData[row][col]);
                if(trLabels[row] == 1){
                    System.out.print("Class: " + trLabels[row] + "     Data: " + trData[row][col] + "       Outcome: ");
                    //check alcoholic < non mean + sd
                    // and alcoholic > non mean - sd
                    //thus falling inside 1sd from opposite values mean
                    if(trData[row][col] < upperboundNon && trData[row][col] > lowerboundNon){
                        System.out.println("true");
                        issues++;
                    }
                    else{
                        System.out.println("false");
                    }
                }
                else{
                    //check non alcoholic < alco mean + sd
                    // and non alcoholic > alco mean - sd
                    //thus falling inside 1sd from opposite values mean
                    System.out.print("Class: " + trLabels[row] + "     Data: " + trData[row][col] + "       Outcome: ");
                    if(trData[row][col] < upperboundAlco && trData[row][col] > lowerboundAlco){
                        System.out.println("true");
                        issues++;
                    }
                    else{
                        System.out.println("false");
                    }
                }
            }
            System.out.println(issues);
            if(issues > 20){
                System.out.println("Deleting column: " + col);
            }
            issues = 0;
        }
    }
}