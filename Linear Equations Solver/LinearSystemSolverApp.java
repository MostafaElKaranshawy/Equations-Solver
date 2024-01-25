import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.EigenDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class LinearSystemSolverApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Linear System Solver");
        VBox mainMenu = createMainMenu(primaryStage);
        ScrollPane scrollPane = new ScrollPane(mainMenu);
        scrollPane.setFitToWidth(true);
        Scene scene = new Scene(new StackPane(scrollPane), 700, 600);
        scene.getRoot().setStyle("-fx-background-color: #f0f0f0;");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    private VBox createMainMenu(Stage primaryStage) {
        VBox menu = new VBox(10);
        menu.setPadding(new Insets(20));
        menu.setStyle("-fx-background-color: #808080; -fx-padding: 10px;");
        TextField variablesField = new TextField();
        variablesField.setPromptText("Enter Number of Variables");
        variablesField.setStyle("-fx-background-color: #ffffff;");
        TextField precisionField = new TextField();
        precisionField.setPromptText("Enter Precision");
        precisionField.setStyle("-fx-background-color: #ffffff;");
        ComboBox<String> methodComboBox = new ComboBox<>();
        methodComboBox.getItems().addAll(
                "Gauss Elimination", "Gauss Jordan Elimination", "Doolittle LU Decomposition", "Crout LU Decomposition","Cholesky Decomposition", "Jacobi Iterative Method","Gauss-Seidel Iterative Method"
        );
        methodComboBox.setPromptText("Select Linear System Solving Method");
        methodComboBox.setStyle("-fx-background-color: #3399ff");
        methodComboBox.setStyle("-fx-background-color: #3399ff;");
        variablesField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                variablesField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            validateNumberOfVariables(variablesField.getText());
        });
        precisionField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                precisionField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            validatePrecision(precisionField.getText());
        });
        GridPane matrixGrid = createMatrixGrid();
        Label iterationsLabel = new Label("Number of Iterations:");
        TextField iterationsField = new TextField();
        iterationsField.setPromptText("Enter Number of Iterations");
        iterationsField.setStyle("-fx-background-color: #ffffff;");
        iterationsField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                iterationsField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            validateIterations(iterationsField.getText());
        });
        Label initialGuessLabel = new Label("Initial Guess:");
        TextField initialGuessField = new TextField();
        initialGuessField.setPromptText("Enter Initial Guess");
        initialGuessField.setStyle("-fx-background-color: #ffffff;");
        Label ErrorToleranceLabel = new Label("ErrorTolerance:");
        TextField ErrorToleranceField = new TextField();
        ErrorToleranceField.setPromptText("Enter ErrorTolerance ");
        ErrorToleranceField.setStyle("-fx-background-color: #ffffff;");
        ErrorToleranceField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                ErrorToleranceField.setText(newValue.replaceAll("[^\\d]", ""));
            }
            validateErrorTolerance(ErrorToleranceField.getText());
        });
        Button solveButton = new Button("Solve");
        Label messageLabel = new Label();
        solveButton.setOnMouseEntered(e -> solveButton.setStyle("-fx-background-color: #45a049; -fx-text-fill: white;"));
        solveButton.setOnMouseExited(e -> solveButton.setStyle("-fx-background-color: #4caf50; -fx-text-fill: white;"));
        TextArea solutionStepsTextArea = new TextArea();
        solutionStepsTextArea.setEditable(false);
        solutionStepsTextArea.setWrapText(true);
        solutionStepsTextArea.setPrefHeight(450);
        methodComboBox.setOnAction(e -> {
            String selectedMethod = methodComboBox.getValue();
            if (selectedMethod != null) {
                try {
                    int numberOfVariables = Integer.parseInt(variablesField.getText());
                    updateMatrixGrid(matrixGrid, numberOfVariables);
                    if (selectedMethod.equals("Jacobi Iterative Method") || selectedMethod.equals("Gauss-Seidel Iterative Method")) {
                        matrixGrid.add(iterationsLabel, 1, numberOfVariables);
                        matrixGrid.add(iterationsField, 2, numberOfVariables);
                        matrixGrid.add(initialGuessLabel, 3, numberOfVariables);
                        matrixGrid.add(initialGuessField, 4, numberOfVariables);
                        matrixGrid.add(ErrorToleranceLabel, 5, numberOfVariables);
                        matrixGrid.add(ErrorToleranceField, 6, numberOfVariables);
                    } else {
                        matrixGrid.getChildren().removeAll(iterationsLabel, iterationsField, initialGuessLabel, initialGuessField, ErrorToleranceLabel, ErrorToleranceField);
                    }
                    handleMethodSelection(primaryStage, selectedMethod, numberOfVariables, matrixGrid,
                            iterationsField.getText(), initialGuessField.getText(), ErrorToleranceField.getText(),solveButton, messageLabel, solutionStepsTextArea);
                    messageLabel.setText("");
                } catch (NumberFormatException ex) {
                    messageLabel.setText("Please enter a valid number for variables.");
                }
            }
        });
        solveButton.setOnAction(e -> {
            String selectedMethod = methodComboBox.getValue();
            int numberOfVariables;
            String errorToleranceText;
            if (selectedMethod != null) {
                try {
                    numberOfVariables = Integer.parseInt(variablesField.getText());
                    double[][] augmentedMatrix = getMatrixValues(matrixGrid, numberOfVariables);
                    int presicion;
                    if (precisionField.getText().isEmpty()) {
                        presicion = 5;
                    } else {
                        presicion = Integer.parseInt(precisionField.getText());
                        System.out.println(presicion);
                    }
                    int iterations;
                    if (iterationsField.getText().isEmpty()) {
                        iterations = 5;
                    } else {
                        iterations = Integer.parseInt(iterationsField.getText());
                    }
                    String  intialGuess = initialGuessField.getText();
                    double ErrorTolerance;
                    System.out.println(selectedMethod);
                    switch (selectedMethod) {
                        case "Gauss Elimination":
                            System.out.println("okkkkk");
                            solveGaussElimination(augmentedMatrix, presicion, solutionStepsTextArea);
                            break;
                        case "Gauss Jordan Elimination":
                            solveGaussJordanElimination(augmentedMatrix,presicion,solutionStepsTextArea);
                            break;
                        case "Doolittle LU Decomposition":
                            solveDoolittleLU(augmentedMatrix,presicion,solutionStepsTextArea);
                            break;
                        case "Crout LU Decomposition":
                            solveCroutLU(augmentedMatrix,presicion, solutionStepsTextArea);
                            break;
                        case "Cholesky Decomposition":
                            solveCholeskyDecomposition(augmentedMatrix,presicion,solutionStepsTextArea);
                            break;
                        case "Jacobi Iterative Method":
                            try {
                                iterations = Integer.parseInt(iterationsField.getText());
                            } catch (NumberFormatException m) {
                                iterations = -1;
                            }
                            intialGuess = initialGuessField.getText();
                            errorToleranceText = ErrorToleranceField.getText();
                            if (errorToleranceText.isEmpty()) {
                                ErrorTolerance = -1.0;
                            }
                            else {
                                try {
                                    ErrorTolerance = Double.parseDouble(errorToleranceText);
                                } catch (NumberFormatException n) {
                                    ErrorTolerance = -1.0;
                                }
                            }
                            if (errorToleranceText.isEmpty() && iterationsField.getText().isEmpty()){
                                showAlert("Error", "Please enter only one of either Error Tolerance or number of iterations" );
                            }
                            if (!errorToleranceText.isEmpty() && !iterationsField.getText().isEmpty()){
                                showAlert("Error", "Please enter only one of either Error Tolerance or number of iterations" );
                            }
                            solveJacobiIterative(augmentedMatrix,presicion, iterations, intialGuess, ErrorTolerance, solutionStepsTextArea);
                            break;
                        case "Gauss-Seidel Iterative Method":
                            try {
                                iterations = Integer.parseInt(iterationsField.getText());
                            } catch (NumberFormatException m) {
                                iterations = -1;
                            }
                            intialGuess = initialGuessField.getText();
                            errorToleranceText = ErrorToleranceField.getText();
                            if (errorToleranceText.isEmpty()) {
                                ErrorTolerance = -1.0;
                            }
                            else {
                                try {
                                    ErrorTolerance = Double.parseDouble(errorToleranceText);
                                } catch (NumberFormatException n) {
                                    ErrorTolerance = -1.0;
                                }
                            }
                            if (errorToleranceText.isEmpty() && iterationsField.getText().isEmpty()){
                                showAlert("Error", "Please enter only one of either Error Tolerance or number of iterations" );
                            }
                            if (!errorToleranceText.isEmpty() && !iterationsField.getText().isEmpty()){
                                showAlert("Error", "Please enter only one of either Error Tolerance or number of iterations" );
                            }
                            solveGaussSeidelIterative(augmentedMatrix,presicion, iterations , intialGuess, ErrorTolerance,  solutionStepsTextArea);
                            break;
                        default:
                            System.out.println("Method not implemented yet.");
                            break;
                    }
                } catch (Exception ex) {
                    messageLabel.setText("Please enter valid numerical values in the matrix.");
                }
            }
        });
        menu.getChildren().addAll(methodComboBox, variablesField, precisionField, matrixGrid, solveButton, messageLabel, solutionStepsTextArea);
        return menu;
    }
    private void validateErrorTolerance(String text) {
        try {
            if (text.isEmpty()) {
                showAlert("Error", "Please enter Error Tolerance");
                return;
            }
            int ErrorTolerance = Integer.parseInt(text);
            if (ErrorTolerance < 0) {
                showAlert("Error", "Error tolerance must be positive real number");
                return;
            }
        } catch (NumberFormatException ex) {
            showAlert("Error", "Please enter a valid Error tolerance");
            return;
        }
    }
    private void validateIterations(String text) {
        try {
            if (text.isEmpty()) {
                showAlert("Error", "Please enter Iterations number");
                return;
            }
            int Iterations  = Integer.parseInt(text);
            if (Iterations  < 2) {
                showAlert("Error", "Iterations number must be at least 1");
                return;
            }
        } catch (NumberFormatException ex) {
            showAlert("Error", "Please enter a valid Iterations number");
            return;
        }
    }
    private void validatePrecision(String text) {
        try {
            if (text.isEmpty()) {
                showAlert("Error", "Please enter presicion");
                return;
            }
            int precision = Integer.parseInt(text);
            if (precision < 1) {
                showAlert("Error", "precision must be at least 1");
                return;
            }
        } catch (NumberFormatException ex) {
            showAlert("Error", "Please enter a valid presicion");
            return;
        }
    }
    private void validateNumberOfVariables(String variablesText) {
        try {
            if (variablesText.isEmpty()) {
                showAlert("Error", "Please enter the number of variables.");
                return;
            }
            int numberOfVariables = Integer.parseInt(variablesText);
            if (numberOfVariables < 2) {
                showAlert("Error", "Number of variables must be at least 2");
                return;
            }
        } catch (NumberFormatException ex) {
            showAlert("Error", "Please enter a valid number for variables.");
            return;
        }
    }
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    private GridPane createMatrixGrid() {
        GridPane matrixGrid = new GridPane();
        matrixGrid.setHgap(10);
        matrixGrid.setVgap(5);
        return matrixGrid;
    }
    private void updateMatrixGrid(GridPane matrixGrid, int numberOfVariables) {
        matrixGrid.getChildren().clear();
        for (int i = 0; i < numberOfVariables; i++) {
            for (int j = 0; j <= numberOfVariables; j++) {
                TextField entry = new TextField();
                if(j < numberOfVariables){
                    entry.setPromptText("x" + (j+1));
                }
                else{
                    entry.setPromptText("c"+(i+1));
                }
                matrixGrid.add(entry, j, i);
            }
        }
    }
    private double[][] getMatrixValues(GridPane matrixGrid, int numberOfVariables) {
        double[][] matrix = new double[numberOfVariables][numberOfVariables + 1];
        for (int i = 0; i < numberOfVariables; i++) {
            for (int j = 0; j <= numberOfVariables; j++) {
                TextField entry = (TextField) matrixGrid.getChildren().get(i * (numberOfVariables + 1) + j);
                String entryText = entry.getText().trim();
                matrix[i][j] = entryText.isEmpty() ? 0 : Double.parseDouble(entryText);
            }
        }
        System.out.println(matrix[0][0]);
        return matrix;
    }
    private void handleMethodSelection(Stage primaryStage, String methodName, int numberOfVariables,
                                       GridPane matrixGrid, String iterations, String initialGuess, String ErrorTolerance,
                                       Button solveButton, Label messageLabel, TextArea solutionStepsTextArea) {
        System.out.println("Matrix Values:");
        for (int i = 0; i < numberOfVariables; i++) {
            for (int j = 0; j <= numberOfVariables; j++) {
                TextField entry = (TextField) matrixGrid.getChildren().get(i * (numberOfVariables + 1) + j);
                System.out.print(entry.getText() + "\t");
            }
            System.out.println();
        }
        System.out.println("Selected Method: " + methodName);
        System.out.println("Number of Variables: " + numberOfVariables);
        if (methodName.equals("Jacobi Iterative Method") || methodName.equals("Gauss-Seidel Iterative Method")) {
            System.out.println("Number of Iterations: " + iterations);
            System.out.println("ErrorTolerance: " + ErrorTolerance);
            String[] initialGuessValues = initialGuess.split("\\s+");
            System.out.println("Initial Guess Values:");
            for (String value : initialGuessValues) {
                System.out.print(value + "\t");
            }
            System.out.println();
        }
        solveButton.setDisable(false);
        solutionStepsTextArea.clear();
    }
    private double[][] scalingMatrix(double[][] augmentedMatrix, int presicion, int step) {
        double[] scaler = new double[augmentedMatrix.length - step];
        for(int i = step; i < augmentedMatrix.length; i++) {
            double maxi = Math.abs(augmentedMatrix[i][i]);
            for(int j = step; j < augmentedMatrix[0].length-1; j++){
                if(Math.abs(augmentedMatrix[i][j]) > Math.abs(maxi))maxi = Math.abs(augmentedMatrix[i][j]);
            }
            scaler[i - step] = Math.abs(augmentedMatrix[i][step]/maxi);
            // System.out.println(scaler[i - step]);
        }
        double maxscaler = scaler[0];
        int maxindex = step;
        for(int i = step; i < augmentedMatrix.length ; i++){
            if(Math.abs(scaler[i-step]) > Math.abs(maxscaler)){maxscaler = Math.abs(scaler[i-step]); maxindex = i;}
        }
        double[] temp = augmentedMatrix[step];
        augmentedMatrix[step] = augmentedMatrix[maxindex];
        augmentedMatrix[maxindex] = temp;
        return augmentedMatrix;
    }
    private void solveGaussElimination(double[][] augmentedMatrix, int presicion , TextArea solutionStepsTextArea) {

        int n = augmentedMatrix.length;

        // Forward Elimination
        for (int pivot = 0; pivot < n - 1; pivot++) {
            // Display the matrix before each step
            augmentedMatrix = scalingMatrix(augmentedMatrix, presicion, pivot);
            displayMatrixAfterStep(augmentedMatrix, solutionStepsTextArea, "Before Step " + (pivot + 1));
            for (int row = pivot + 1; row < n; row++) {
                double factor = augmentedMatrix[row][pivot] / augmentedMatrix[pivot][pivot];
                // Check is there is a solution.
                // if(augmentedMatrix[pivot][pivot] == 0){
                //     return;
                // }
                for (int col = pivot; col <= n; col++) {
                    augmentedMatrix[row][col] -= factor * augmentedMatrix[pivot][col];
                    augmentedMatrix[row][col] = formatWithSignificantFigures(augmentedMatrix[row][col],presicion);
                }
            }
        }

        // Display the matrix after forward elimination
        displayMatrixAfterStep(augmentedMatrix, solutionStepsTextArea, "After Forward Elimination");

        // Back Substitution
        /*
        for (int row = n - 1; row >= 0; row--) {
            solution[row] = formatWithSignificantFigures(augmentedMatrix[row][n] / augmentedMatrix[row][row], presicion);
            for (int col = row + 1; col < n; col++) {
                solution[row] -= augmentedMatrix[row][col] * solution[col];
                solution[row] = formatWithSignificantFigures(solution[row], presicion);
            }
        }
        */

        double[] solution = new double[n];
        double[][] A = new double[n][n+1];
        double[] B = new double[n];
        for(int i = 0 ; i < augmentedMatrix.length ; ++i){
            for (int j = 0 ; j < augmentedMatrix[0].length ; ++j){
                if(j < augmentedMatrix[0].length-1 ){
                    A[i][j] =  augmentedMatrix[i][j] ;
                }
                else
                    B[i] = augmentedMatrix[i][j] ;
            }
        }
        solution = backwardSubstitution(A, B, presicion);
        // Display the final solution in the text area
        solutionStepsTextArea.appendText("\nFinal Solution:\n");
        for (int i = 0; i < n; i++) {
            solutionStepsTextArea.appendText("x" + (i + 1) + " = " + solution[i] + "\n");
        }
    }

    /*
    private boolean validateInitialGuess(String initialGuess, int numberOfVariables) {
        String[] guessValues = initialGuess.split("\\s+");

        if (guessValues.length != numberOfVariables) {
            showAlert("Error", "Invalid number of values in the initial guess. Please provide " + numberOfVariables + " values.");
            return false;
        }

        for (String value : guessValues) {
            try {
                Double.parseDouble(value);
            } catch (NumberFormatException e) {
                showAlert("Error", "Invalid initial guess value. Please enter real numbers separated by spaces.");
                return false;
            }
        }

        return true;
    }
    */
    private void solveGaussJordanElimination(double[][] augmentedMatrix, int presicion , TextArea solutionStepsTextArea) {
        System.out.println(presicion);

        double [][] A = new double[augmentedMatrix.length][augmentedMatrix[0].length-1] ;
        double [] B = new double[augmentedMatrix.length] ;




        // Elimination
        int rows = augmentedMatrix.length;
        int cols = augmentedMatrix[0].length;
        for (int i = 0; i < rows; i++) {
            // scaling the matrix
            augmentedMatrix = scalingMatrix(augmentedMatrix, presicion, i);
            // Normalize the current row
            double divisor = augmentedMatrix[i][i];
            // check is there is a solution
            if(divisor == 0){
                if(augmentedMatrix[i][cols-1] == 0)solutionStepsTextArea.appendText("The equation has infinite number of solution");
                else solutionStepsTextArea.appendText("The equation has No solution");
                return;
            }
            for (int j = 0; j < cols; j++) {
                augmentedMatrix[i][j] /= divisor;
                augmentedMatrix[i][j] = formatWithSignificantFigures(augmentedMatrix[i][j], presicion);
            }
            displayMatrixAfterStep(augmentedMatrix, solutionStepsTextArea, "Before Step " + (i + 1));
            // Make other rows zero in the current column
            for (int k = 0; k < rows; k++) {

                if (k != i) {
                    double factor = augmentedMatrix[k][i];
                    for (int j = 0; j < cols; j++) {
                        augmentedMatrix[k][j] -= factor * augmentedMatrix[i][j];
                        augmentedMatrix[k][j] = formatWithSignificantFigures(augmentedMatrix[k][j], presicion);
                    }
                }
            }
        }
        displayMatrixAfterStep(augmentedMatrix, solutionStepsTextArea, "After Forward Elimination");



        // Get Solution
        double[] solution = new double[rows];
        for(int i = 0; i < rows; i++){
            solution[i] = formatWithSignificantFigures(augmentedMatrix[i][rows],presicion);
        }


        // Display the final solution in the text area
        solutionStepsTextArea.appendText("\nFinal Solution:\n");
        for (int i = 0; i < rows; i++) {
            solutionStepsTextArea.appendText("x" + (i + 1) + " = " + solution[i] + "\n");
        }
    }



    private void solveDoolittleLU(double [][] augmentedMatrix , int presicion, TextArea solutionStepsTextArea  ){

        double [][] A = new double[augmentedMatrix.length][augmentedMatrix[0].length-1] ;
        double [] B = new double[augmentedMatrix.length] ;
        int [] P = new int[augmentedMatrix.length] ;
        for(int i = 0 ; i < augmentedMatrix.length ; ++i){
            for (int j = 0 ; j < augmentedMatrix[0].length ; ++j){
                if(j < augmentedMatrix[0].length-1 ){
                    A[i][j] =  augmentedMatrix[i][j] ;
                }
                else
                    B[i] = augmentedMatrix[i][j] ;
            }
            P[i] = i ;
        }
        try {
            double[][][] result = luDecomposition2(A, P, presicion, B, solutionStepsTextArea);
            if (result == null) {
                showAlert("Error","det()=0 or near to to be zero");
                return;
            }

            Boolean case1 = true;
            int len = augmentedMatrix.length;
            if (Math.abs(result[1][len - 1][len - 1]) <= 1E-12)
                case1 = false;
            double[] y = forwardSubstitution(result[0], B, presicion);


            if (!case1) {
                showAlert("Error","det()=0 or near to to be zero");
                return;
            }
            //double[] y = forwardSubstitution(result[0], B, presicion);
            double[] x = backwardSubstitution(result[1], y, presicion);
            print(result[0]);
            print(result[1]);


            //Andrew Edittttttttttt
            int m = 0;
            solutionStepsTextArea.appendText("\nAfter Solving Ly = b\n");
            for (double value : y) {
                m++;
                System.out.print(value + " ");
                solutionStepsTextArea.appendText("y" + (m) + " = " + value + "\n");
            }
            // end of andrew edit


            // Display the solution
            System.out.print("Solution: ");
            int l =0;
            solutionStepsTextArea.appendText("\nAfter Solving Ux = y\n");
            for (double value : x) {
                l++;
                System.out.print(value + " ");
                solutionStepsTextArea.appendText("x" + (l) + " = " + value + "\n");
            }
        }catch (Exception e){
            showAlert("Error","det()=0 or near to to be zero");
            return;
        }
    }

    public  void print(double [][] a){
        for(double [] d : a){
            for (double i : d )
                System.out.print(i+"  ");
            System.out.println();
        }
    }

    private  double[][][] luDecomposition2(double [][]A , int [] P , int presicion , double [] B,  TextArea solutionStepsTextArea) {
        System.out.println("precision = " + presicion);
        int n = A.length;
        double[][] L = new double[n][n];
        double[][] U = new double[n][n];


        for (int k = 0; k < n - 1; k++) {
            int pivotRow = findPivotRow(A, P, k);
            if(pivotRow == -1)
                return null;
            P = swapRows(P, k, pivotRow , B);
            for (int i = k + 1; i < n; i++) {
                if(A[P[k]][k] ==0)
                    return null;
                double factor = formatWithSignificantFigures( A[P[i]][k] / A[P[k]][k] ,presicion);
                A[P[i]][k] = factor;
                for (int j = k + 1; j < n; j++) {
                    A[P[i]][j] = formatWithSignificantFigures (A[P[i]][j] - formatWithSignificantFigures (factor * A[P[k]][j] ,presicion),presicion);
                }
            }
        }

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i > j) {
                    L[i][j] = A[P[i]][j];
                    U[i][j] = 0;
                } else if (i == j) {
                    L[i][j] = 1;
                    U[i][j] = A[P[i]][j];
                } else {
                    L[i][j] = 0;
                    U[i][j] = A[P[i]][j];
                }

                //displayMatrixAfterStep2("Matrix U:", U, solutionStepsTextArea);
                //displayMatrixAfterStep2("Matrix L:", L, solutionStepsTextArea);

            }
            solutionStepsTextArea.appendText("\n" + "step "+ (i+1)+"\n");
            displayMatrixAfterStep2("Matrix L:", L, solutionStepsTextArea);
            displayMatrixAfterStep2("Matrix U:", U, solutionStepsTextArea);
        }
        return new double[][][]{L, U};
    }

    private void displayMatrixAfterStep2(String title, double[][] matrix, TextArea solutionStepsTextArea) {
        solutionStepsTextArea.appendText(title + "\n");
        for (double[] row : matrix) {
            for (double value : row) {
                solutionStepsTextArea.appendText(String.format("%.5f\t", value));
            }
            solutionStepsTextArea.appendText("\n");
        }
        solutionStepsTextArea.appendText("\n");
    }

    private  int findPivotRow(double[][] A, int[] P, int k) {
        int pivotRow = k;
        double max = Math.abs(A[P[k]][k]);

        for (int i = k + 1; i < A.length; i++) {
            double absValue = Math.abs(A[P[i]][k]);
            if (absValue > max) {
                max = absValue;
                pivotRow = i;
            }
        }
        if(max == 0)
            return -1;

        return pivotRow;
    }
    private  int[] swapRows(int[] P, int row1, int row2 , double [] B) {
        int temp = P[row1];
        P[row1] = P[row2];
        P[row2] = temp;
        double temp2 = B[row1] ;
        B[row1] = B[row2];
        B[row2] = temp2;

        return P ;
    }


    private  double[] forwardSubstitution(double[][] L , double [] B ,int presicion) {
        int n = L.length;
        double[] y = new double[n];

        for (int i = 0; i < n; i++) {
            y[i] = B[i];
            for (int j = 0; j < i; j++) {
                y[i] = formatWithSignificantFigures (y[i] - formatWithSignificantFigures( L[i][j] * y[j] ,presicion ),presicion);
            }
        }

        return y;
    }

    private  double[] backwardSubstitution(double[][] U, double[] y , int presicion) {
        int n = U.length;
        double[] x = new double[n];

        for (int i = n - 1; i >= 0; i--) {
            x[i] = y[i];
            for (int j = n - 1; j > i; j--) {
                x[i] =  formatWithSignificantFigures (x[i] -  formatWithSignificantFigures (U[i][j] * x[j] ,presicion),presicion);
            }
            x[i] =formatWithSignificantFigures( x[i] /U[i][i] ,presicion) ;
        }

        return x;
    }
    private static  double formatWithSignificantFigures(double number , int presicion) {
        BigDecimal bd = new BigDecimal(number);

        // Determine the scale based on the significant figures
        int scale = presicion - bd.precision() + bd.scale();

        // Round the BigDecimal to the determined scale
        bd = bd.setScale(scale, RoundingMode.HALF_UP);

//        // Convert the rounded BigDecimal to a string

        return Double.parseDouble(bd.stripTrailingZeros().toPlainString()) ;
    }

    private void solveCroutLU(double[][] augmentedMatrix, int presicion , TextArea solutionStepsTextArea) {
        int n = augmentedMatrix.length;
        double sum;
        double[][] L = new double[n][n];
        double[][] U = new double[n][n];

        for (int i = 0; i < n; i++) {
            U[i][i] = 1;
        }

        for (int j = 0; j < n; j++) {
            for (int i = j; i < n; i++) {
                sum = 0;
                for (int k = 0; k < j; k++) {
                    sum += formatWithSignificantFigures(L[i][k] * U[k][j], presicion);
                }
                L[i][j] = formatWithSignificantFigures(augmentedMatrix[i][j] - sum, presicion);
            }
            //printMatrix(L, "L after step " + (j + 1));
            solutionStepsTextArea.appendText("\n" + "step "+ (j+1)+"\n");
            displayMatrixAfterStep2("Matrix L:", L, solutionStepsTextArea);

            for (int i = j; i < n; i++) {
                sum = 0;
                for (int k = 0; k < j; k++) {
                    sum += formatWithSignificantFigures(L[j][k] * U[k][i], presicion);
                }
                if (L[j][j] == 0) {
                    System.out.println("det(L) close to 0!\n Can't divide by 0...");
                    showAlert("Error", "det(L) close to 0!\\n" + " Can't divide by 0..." );
                }
                U[j][i] = formatWithSignificantFigures(formatWithSignificantFigures(augmentedMatrix[j][i] - sum ,presicion )/ L[j][j], presicion);
            }
            //printMatrix(U, "U after step " + (j + 1));
            displayMatrixAfterStep2("Matrix U:", U, solutionStepsTextArea);
        }

        double[] B = new double[n];
        for (int i = 0; i < n; i++) {
            B[i] = augmentedMatrix[i][n];
        }

        //displayMatrixAfterStep2("Matrix L:", L, solutionStepsTextArea);
        //displayMatrixAfterStep2("Matrix U:", U, solutionStepsTextArea);
        int u = 0, q = 0;
        solutionStepsTextArea.appendText("\nAfter Solving Ly = b\n");
        double[] y = forwardSubstitution2(L, B, presicion);
        for (double value : y) {
            q++;
            System.out.print(value + " ");
            solutionStepsTextArea.appendText("y" + (q) + " = " + value + "\n");
        }
        solutionStepsTextArea.appendText("\nAfter Solving Ux = y\n");
        double[] x = backwardSubstitution2(U, y, presicion);
        for (double value : x) {
            u++;
            System.out.print(value + " ");
            solutionStepsTextArea.appendText("x" + (u) + " = " + value + "\n");
        }

    }
    private static double[] forwardSubstitution2(double[][] L, double[] B, int precision) {
        int n = L.length;
        double[] y = new double[n];

        for (int i = 0; i < n; i++) {
            y[i] = B[i];
            for (int j = 0; j < i; j++) {
                y[i] =formatWithSignificantFigures(formatWithSignificantFigures(y[i],precision)- formatWithSignificantFigures(L[i][j] * y[j], precision),precision);
            }
            y[i] =formatWithSignificantFigures(y[i] /L[i][i],precision);
        }
        return y;
    }

    private static double[] backwardSubstitution2(double[][] U, double[] y, int precision) {
        int n = U.length;
        double[] x = new double[n];

        for (int i = n - 1; i >= 0; i--) {
            x[i] = y[i];
            for (int j = i + 1; j < n; j++) {
                x[i] = formatWithSignificantFigures(formatWithSignificantFigures(x[i],precision)-formatWithSignificantFigures(U[i][j] * x[j], precision),precision);
            }
            x[i] =formatWithSignificantFigures(x[i]/U[i][i],precision) ;
        }
        return x;
    }

    private void solveCholeskyDecomposition(double[][] augmentedMatrix,int presicion , TextArea solutionStepsTextArea) {
        double[][] A = new double[augmentedMatrix.length][augmentedMatrix[0].length-1];
        for(int i = 0 ; i < augmentedMatrix.length; i++){
            for(int j = 0 ; j < augmentedMatrix[0].length-1; j++){
                A[i][j] = augmentedMatrix[i][j];
            }
        }
        if(squareCheck(A) && transposeCheck(A) && checkPositiveDefinite(A)){
            double[][][] answer = findCholesky(A, presicion, solutionStepsTextArea);
            displayMatrixAfterStep2("Matrix L:", answer[0], solutionStepsTextArea);
            displayMatrixAfterStep2("Matrix Lᵀ:", answer[1], solutionStepsTextArea);
        }
        else{
            showAlert("Error","This matrix cannot be decomposed by Cholesky's method");
        }
    }

    private boolean squareCheck(double[][] A){
        if(A.length == A[0].length)
            return true;
        return false;
    }

    private boolean transposeCheck(double[][] A){
        for (int i = 0; i < A.length; i++) {
            for (int j = 0; j < A[0].length; j++) {
                if (A[i][j] - A[j][i] >= 1e-5) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean checkPositiveDefinite(double[][] A){
        boolean definite = false;
        RealMatrix matrix = new Array2DRowRealMatrix(A);

        // Create an EigenDecomposition object
        EigenDecomposition decomposition = new EigenDecomposition(matrix);

        // Get the real parts of the eigenvalues
        double[] realEigenvalues = decomposition.getRealEigenvalues();

        // Print the eigenvalues
        for(int i = 0 ; i < realEigenvalues.length ; i++){
            if(realEigenvalues[i] > 0)
                definite = true;
            else if(realEigenvalues[i] < 1e-5 && realEigenvalues[i] > -(1e-5))
                continue;
            else
                return false;
        }
        return definite;
    }

    private double[][][] findCholesky(double[][] A, int precision, TextArea solutionStepsTextArea){
        double[][] lowerTriangular = new double[A.length][A.length];
        double[][] upperTriangular = new double[A.length][A.length];
        double[][][] solution = new double[2][A.length][A.length];
        for(int k = 0 ; k < A.length; k++){
            for(int i = 0 ; i < A.length; i++){
                double sum = 0;
                if(i < k){
                    for(int j = 0; j < i; j++){
                        sum += formatWithSignificantFigures(lowerTriangular[i][j] *
                                lowerTriangular[k][j], precision);
                    }
                    lowerTriangular[k][i] = formatWithSignificantFigures((A[k][i] - sum) / lowerTriangular[i][i], precision);
                } else if(k == i){
                    for(int j = 0 ; j < k; j++){
                        sum += formatWithSignificantFigures(Math.pow(lowerTriangular[k][j], 2), precision);
                    }
                    lowerTriangular[k][k] = formatWithSignificantFigures(Math.sqrt(A[k][k] - sum), precision);
                }else{
                    lowerTriangular[k][i] = 0;
                }
            }
            displayMatrixAfterStep2("Lower triangular Matrix after step "+ (k+1) + ":", lowerTriangular, solutionStepsTextArea);
        }

        for (int i = 0; i < upperTriangular.length; i++) {
            for (int j = 0; j < upperTriangular.length; j++) {
                upperTriangular[i][j] = lowerTriangular[j][i];
            }
        }
        displayMatrixAfterStep2("Getting the matrix transpose (Lᵀ):", upperTriangular, solutionStepsTextArea);
        solution[0] = lowerTriangular;
        solution[1] = upperTriangular;
        return solution;
    }

    private void displayMatrixAfterStep3(double[] matrix, TextArea solutionStepsTextArea, String step) { //for displaying vectors
        solutionStepsTextArea.appendText("\n" + step + ":\n");
        int i = 0;
        for (double row : matrix) {
            i ++;
            solutionStepsTextArea.appendText("X" + i +String.format("%.5f\t", row));
            solutionStepsTextArea.appendText("\n");
        }
    }

    private void displayMatrixAfterStep(double[][] matrix, TextArea solutionStepsTextArea, String step) {
        solutionStepsTextArea.appendText("\n" + step + ":\n");
        for (double[] row : matrix) {
            for (double value : row) {
                solutionStepsTextArea.appendText(String.format("%.5f\t", value));
            }
            solutionStepsTextArea.appendText("\n");
        }
    }

    private void printVariablesAfterStep(double[] variables, int iteration, TextArea solutionStepsTextArea) {
        System.out.println("\nAfter Iteration " + iteration + ":\n");
        solutionStepsTextArea.appendText("\nIteration " + (iteration) + "\n");
        for (int i = 0; i < variables.length; i++) {
            solutionStepsTextArea.appendText("x" + (i + 1) + " = " + variables[i] + "\n");
        }
    }

    private void gaussSeidelWithIterations(double[][] augmentedMatrix, double precision, int iterations, double[] currentGuess, TextArea solutionStepsTextArea) {
        int n = augmentedMatrix.length;
        for (int iteration = 1; iteration <= iterations; iteration++) {
            for (int i = 0; i < n; i++) {
                double sum = formatWithSignificantFigures(augmentedMatrix[i][n], (int) precision);

                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        sum -= formatWithSignificantFigures(augmentedMatrix[i][j] * currentGuess[j], (int) precision);
                        sum = formatWithSignificantFigures(sum, (int) precision);
                    }
                }
                currentGuess[i] = formatWithSignificantFigures(sum / augmentedMatrix[i][i], (int) precision);
            }
            printVariablesAfterStep(currentGuess, iteration,  solutionStepsTextArea);
        }
    }

    private void gaussSeidelWithTolerance(double[][] augmentedMatrix, double precision, double ErrorTolerance, double[] currentGuess, TextArea solutionStepsTextArea){
        int n = augmentedMatrix.length;
        double[] relativeError = new double[n];
        double[] previousGuess = new double[n];
        previousGuess = Arrays.copyOf(currentGuess, n);
        boolean toleranceAchieved = false;
        int iterations = 1;
        for (; iterations <= 50; iterations++) {
            for (int i = 0; i < n; i++) {
                double sum = formatWithSignificantFigures(augmentedMatrix[i][n], (int) precision);
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        sum -= formatWithSignificantFigures(augmentedMatrix[i][j] * currentGuess[j], (int) precision);
                        sum = formatWithSignificantFigures(sum, (int) precision);
                    }
                }
                currentGuess[i] = formatWithSignificantFigures(sum / augmentedMatrix[i][i], (int) precision);
            }

            for(int k = 0; k < n; k++){
                relativeError[k] = formatWithSignificantFigures(Math.abs( (currentGuess[k] - previousGuess[k]) / currentGuess[k] ) * 100, (int) precision);
                solutionStepsTextArea.appendText("\nRelative error X" + (k+1) +" (in %) : " + (relativeError[k]));
            }

            for(int k = 0 ; k < n ; k++){
                if(relativeError[k] < ErrorTolerance) {
                    if(k == n-1 ){toleranceAchieved = true; }
                } else {
                        break;
                }
            }
            previousGuess = Arrays.copyOf(currentGuess, n);
            printVariablesAfterStep(currentGuess, iterations,  solutionStepsTextArea);
            if (toleranceAchieved)
                break;

            if(iterations == 50)
                showAlert("Error","Couldn't Converge");
        }
    }
    private boolean checkForDiagonallyDominant(double[][] augmentedMatrix){
        boolean oneOfThemGreater = false;
        boolean greaterThanOrEqual = true;
        int n = augmentedMatrix.length;
        for (int i = 0; i < n; i++){
            double pivot = Math.abs(augmentedMatrix[i][i]);
            double sum = 0;
            for (int j = 0; j < n; j++){
                if (i != j)
                    sum += Math.abs(augmentedMatrix[i][j]);
            }
            if (pivot > sum)
                oneOfThemGreater = true;
            else if(pivot < sum){
                greaterThanOrEqual = false;
                break;
            }
        }
        return (!oneOfThemGreater || !greaterThanOrEqual);
    }

    private void solveGaussSeidelIterative(double[][] augmentedMatrix, double precision, int iterations, String initialGuess, double ErrorTolerance, TextArea solutionStepsTextArea) {
        int n = augmentedMatrix.length;
        String[] initialGuessValues = initialGuess.split("\\s+");
        double[] currentGuess = new double[]{1, 0, 1};
        if (initialGuessValues.length != n) {
            showAlert("Error", "Please enter a valid initial Guess");
            return;
        }
        for (int i = 0; i < n; i++) {
            if (augmentedMatrix[i][i] == 0) {
                showAlert("Error", "can't use gauss seidel because the main diagonal contains zeros");
                System.out.println("zero found at " + i );
                return;
            }
        }
        //check diagonally dominant
        if (checkForDiagonallyDominant(augmentedMatrix)){
            showAlert("Error", "not diagonally dominant");
            System.out.println("not diagonally dominant");
            return;
        }
        for (int i = 0; i < n; i++) {
            try {
                currentGuess[i] = Double.parseDouble(initialGuessValues[i]);
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid initial guess value at index " + i);
                showAlert("Error", "Enter valid initial guess");
                return;
            }
        }
        if(iterations != -1){
            gaussSeidelWithIterations(augmentedMatrix, precision, iterations, currentGuess, solutionStepsTextArea);
        }
        else{
            gaussSeidelWithTolerance(augmentedMatrix, precision, ErrorTolerance, currentGuess, solutionStepsTextArea);
        }
    }
    private void jacobiWithIterations(double[][] augmentedMatrix, double precision, int iterations, double[] currentGuess, TextArea solutionStepsTextArea){
        int n = augmentedMatrix.length;
        double[] tempGuess = new double[n];
        System.out.println(Arrays.toString(currentGuess)); //received correctly
        for (int iteration = 1; iteration <= iterations; iteration++) {
            int i = 0;
            double sum = 0;
            for (; i < n; i++) {
                sum = formatWithSignificantFigures(augmentedMatrix[i][n], (int) precision);

                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        sum -= formatWithSignificantFigures(augmentedMatrix[i][j] * currentGuess[j], (int) precision);
                        sum = formatWithSignificantFigures(sum, (int) precision);
                    }
                }
                tempGuess[i] = formatWithSignificantFigures(sum / augmentedMatrix[i][i], (int) precision);
            }
            currentGuess = Arrays.copyOf(tempGuess, n);
            printVariablesAfterStep(currentGuess, iteration,  solutionStepsTextArea);
        }
    }
    private void jacobiWithTolerance(double[][] augmentedMatrix, double precision, double ErrorTolerance, double[] currentGuess, TextArea solutionStepsTextArea){
        int n = augmentedMatrix.length;
        double[] previousGuess = new double[n];
        double[] tempGuess = new double[n];
        boolean toleranceAchieved = false;
        double[] relativeError = new double[n];
        previousGuess = Arrays.copyOf(currentGuess, n);
        for (int iterations = 1; iterations <= 50; iterations++) {
            double sum = 0;
            int i = 0;
            for (; i < n; i++) {
                sum = formatWithSignificantFigures(augmentedMatrix[i][n], (int) precision);
                for (int j = 0; j < n; j++) {
                    if (i != j) {
                        sum -= formatWithSignificantFigures(augmentedMatrix[i][j] * currentGuess[j], (int) precision);
                        sum = formatWithSignificantFigures(sum, (int) precision);
                    }
                }
                tempGuess[i] = formatWithSignificantFigures(sum / augmentedMatrix[i][i], (int) precision);
            }

            currentGuess = Arrays.copyOf(tempGuess, n);

            //check relative error
            for(int k = 0; k < n; k++){
                relativeError[k] = formatWithSignificantFigures(Math.abs( (currentGuess[k] - previousGuess[k]) / currentGuess[k] ) * 100, (int) precision);
                solutionStepsTextArea.appendText("\nRelative error X" + (k+1) +" (in %) : " + (relativeError[k]));
            }

            for(int k = 0 ; k < n ; k++){
                if(relativeError[k] < ErrorTolerance) {
                    if(k == n-1 ){toleranceAchieved = true; }
                } else {
                    break;
                }
            }
            previousGuess = Arrays.copyOf(currentGuess, n);
            printVariablesAfterStep(currentGuess, iterations,  solutionStepsTextArea);
            if (toleranceAchieved)
                break;

            if(iterations == 50)
                showAlert("Error","Couldn't Converge");
        }
    }


    private void solveJacobiIterative(double[][] augmentedMatrix, double precision, int iterations, String initialGuess, double ErrorTolerance, TextArea solutionStepsTextArea){
        int n = augmentedMatrix.length;
        String[] initialGuessValues = initialGuess.split("\\s+");
        double[] currentGuess = new double[n];
        if (initialGuessValues.length != n) {
            showAlert("Error", "Please enter a valid initial Guess");
            return;
        }
        for (int i = 0; i < n; i++) {
            if (augmentedMatrix[i][i] == 0) {
                showAlert("Error", "can't use gauss seidel because the main diagonal contains zeros");
                System.out.println("zero found at " + i );
                return;
            }
        }

        if (checkForDiagonallyDominant(augmentedMatrix)){
            showAlert("Error", "not diagonally dominant");
            System.out.println("not diagonally dominant");
            return;
        }

        for (int i = 0; i < n; i++) {
            try {
                currentGuess[i] = Double.parseDouble(initialGuessValues[i]);
            } catch (NumberFormatException e) {
                System.out.println("Error: Invalid initial guess value at index " + i);
                showAlert("Error", "Enter valid initial guess");
                return;
            }
        }
        if(iterations != -1){
            jacobiWithIterations(augmentedMatrix, precision, iterations, currentGuess, solutionStepsTextArea);
        }
        else{
            jacobiWithTolerance(augmentedMatrix, precision, ErrorTolerance, currentGuess, solutionStepsTextArea);
        }
    }
}