import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/*
 * Working on update: be able to create a super menu that allows you to select from other versions of the Gale Shapley algorithm, create custom 
 *  gale-Shapley algorithms  
 */
public class GaleShapleyV1 {
	public static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
	
	public static void displayMenu(){
		System.out.println("JAVA STABLE MARRIAGE PROBLEM v3\n");// title
		System.out.println("L - Load students and schools from file");	
		System.out.println("E - Edit students and schools");
		System.out.println("P - Print students and schools");
		System.out.println("M - Match students and schools using Gale-Shapley algorithm");
		System.out.println("D - Display matches");
		System.out.println("X - Compare student-optimal and school-optimal matches");
		System.out.println("R - Reset database");
		System.out.println("Q - Quit\n");
		
	}
	
	public static void displayEditMenu(){//Function that displays the edit menu
		System.out.println("Edit data");
		System.out.println("---------");
		System.out.println("S - Edit students\nH - Edit high schools\nQ - Quit");
	}//end displayEditMenu
	
	public static void clearDatabases(ArrayList<School> schoolArray, ArrayList<Student> studentArray){// Helper function to clear the student and school arrays
		clearSchoolDatabase(schoolArray);
		clearStudentDatabase(studentArray);
		
		System.out.println("\nDatabase cleared!\n");
	}//end clearDatabases
	
	public static void clearSchoolDatabase(ArrayList<School> schoolArray){//Function that erases all students and schools
			schoolArray.clear();
	}//end clearSchoolDatabase
	
	public static void clearStudentDatabase(ArrayList<Student> studentArray){
			studentArray.clear();
	}//end clearStudentDatabase
	
	public static void clearSchoolsStudentRankings(ArrayList<School> H, int numSchools){
		for (int i = 0; i < numSchools; i++){//Loop through each School object in the School array
			if (H.get(i) != null){//Check if there is a school at the current index position
				H.get(i).setNParticipants(0);//Set each ranking array size to 0, "erasing" the school's students rankings
			}
			else break; 
			}						   
	}//end clearSchoolsStudentsRankings
	
	public static void clearAssignedStudents(ArrayList<School> H){//clear any previous assignments of students in school objects
		for (int hc = 0; hc < H.size(); hc++){
				H.get(hc).setRegret(-1); // Reset regret values to -1 
				H.get(hc).clearMatches();// Reset student values to default value of -1
				H.get(hc).setFreeStatus(true); // School is free, and is not matched with a student
		}
	}//end clearAssignedStudents
	
	public static void clearAssignedSchools(ArrayList<Student> S){//clear any previous assignments of schools to student objects
		for (int sc = 0; sc < S.size(); sc++){
				S.get(sc).clearMatches();
				S.get(sc).setRegret(-1); //Set the regret back to its default value 
				S.get(sc).setFreeStatus(true); // Student is free and not matched with a school anymore
				S.get(sc).setNextRankToProposeTo(1); // reset the proposal rank
		}
	}//end clearAssignedSchools
	
	public static void clearAssignedParticipants(ArrayList<? extends Participant> P){
		for (int pc = 0; pc < P.size(); pc++){
			P.get(pc).clearMatches();
			P.get(pc).setRegret(-1);
			P.get(pc).setFreeStatus(true);
			P.get(pc).setNextRankToProposeTo(1);
		}
	}
	//Returns index of receiver that matches the rank
	public static int receiverIndexOfRank(Participant participant, int rank){
		for (int rc = 0; rc < participant.getNParticipants(); rc++){
			if (participant.getRanking(rc) == rank)
					return rc;
		}
		return 0;
	}//end receiverIndexOfRank
				
	//Update each school object's rankings and composite scores array to hold appropriate number of student rankings and scores
	public static void updateNumStudents(ArrayList<School> H, int numStudents){
		for (int i = 0; i < H.size(); i++){
			if (H.get(i) != null)
				H.get(i).setNParticipants(numStudents);
		}
	}//end updateNumStudents
	
	public static void schoolsRankStudents(ArrayList<Student> S, ArrayList<School> H, int numSchools){//Helper function for calculating high school rankings of students
		for (int hc = 0; hc < numSchools; hc++){
			H.get(hc).resetRankings(); //Reset the rankings to be safe
			H.get(hc).calcRankings(S); //Calculate them again
			if (H.get(hc).getRegret() != -1)// If regret has been previously calculated
				H.get(hc).calcRegret();// Recalculate it 
		}
	}
	
	//helper function that loads students and schools and deals with effects of adding either
	public static boolean loadParticipants(ArrayList<Student> S, ArrayList<School> H) throws IOException{
		boolean participantsAdded = false;
		
		if (loadSchools(H) > 0){ // If any new schools are added
			clearStudentDatabase(S); // Clear previous students
			clearAssignedStudents(H);// Clear any previously assigned students if they exist
			participantsAdded = true; 
		}
		if (loadStudents(S, H) > 0 ){// If any new students are added
			updateNumStudents(H, S.size()); //Update the rankings array of the schools to hold the new amount of students
			clearAssignedStudents(H); //Clear any previous assignments and regrets of both students and schools
			clearAssignedSchools(S);
			schoolsRankStudents(S,H, H.size()); //Update each schools rankings of the students 
			participantsAdded = true;
			}
		
		System.out.println();
		
		return participantsAdded;
	}//end loadParticipants
	
	//Load student information from a user-provided file and return the number of new students.
	//New students are added to the list of existing students
	public static int loadStudents(ArrayList<Student> S, ArrayList<School> H) throws IOException{
		String userInput = "initialized";
		int potentialStudents = 0, studentsAdded = 0, currentStudents = S.size(), numSchools = H.size();
		boolean validFile = false;
		
		do{
			System.out.print("\nEnter student file name (0 to cancel): ");
			userInput = reader.readLine();
			

			if (userInput.equals("0")){
				System.out.println("\nFile loading process canceled.");
				return 0;
			}
			else {
				File file = new File(userInput);
				if ((!(file.exists())) || (!(file.isFile()))){// If the file name doesn't exist or is not a file
					System.out.println("\nERROR: File not found!");
					validFile = false;
				}
				else {
					validFile = true;
					BufferedReader fileReader = new BufferedReader(new FileReader(userInput));	
					String dataPoints = "initialized";
					
					do{
						dataPoints = fileReader.readLine();
						if (dataPoints != null){
							potentialStudents++;
							String[] splitData = dataPoints.split(",");
							
							if (splitData.length == 3 + numSchools){ // Check if valid number of arguments (name, GPA, ES score, and the number of schools)
								Student tempStudent = new Student(splitData[0], Double.parseDouble(splitData[1]), Integer.parseInt(splitData[2]), numSchools);		
								
								if (checkNegativeIndices(splitData, 3, numSchools) && checkRepeats(splitData, 3, numSchools)){// Check the indices in the data 
									for (int i = 3, rank = 1; i < splitData.length; i++){ // Add the data points to the rankings array in the tempStudent object
										tempStudent.setRanking(Integer.parseInt(splitData[i])- 1, rank);
										rank++; // increment rank so next school receives appropriate rank
									}
									if (tempStudent.isValid()){// If the temporary student object passes all tests
										S.add(currentStudents + studentsAdded, tempStudent); // Add at the next index in the suitor array
										studentsAdded++;
									}
								}
							
							}
						}
					}while (dataPoints != null);
					fileReader.close();
				}
				
			}
		}while (!validFile);
		
		if (studentsAdded > 0)// If any students are added
			clearAssignedSchools(S); // Clear the students assigned schools and regrets since they are not valid anymore 
		
		System.out.format("\n%d of %d students loaded!\n", studentsAdded, potentialStudents);
		
		return studentsAdded;
	}//end loadStudents
	
	//function that checks if the any indices provided in the data files are out of range (i.e. not in 0 - numSchools-1
	public static boolean checkNegativeIndices(String[] data, int startingPoint, int numSchools){
		for (int j = startingPoint; j < data.length; j++){
			int schoolIndex = Integer.parseInt(data[j]);
			//If the index is out of range
			if (!(schoolIndex > 0 && schoolIndex <= numSchools))
				return false;// return false
		}
		return true;
	}//end checkValidSchoolIndices
	
	//Method that checks if any indices are repeated in the data line
	public static boolean checkRepeats(String[] data, int startingPoint, int numSchools){
		int[] array = new int[numSchools];
		
		for (int j = startingPoint; j < data.length; j++){
			array[j-startingPoint] = Integer.parseInt(data[j]);
		}// create an integer array that holds the values for school preference
		
		for (int i = 0; i < array.length; i++){//check if any of those preferences repeat
			if (countInArray((i+1), array) > 1)
				return false;
		}
		return true;
	}//end checkRepeats
	
	//Function that checks if a value is repeated more than once in an array
	public static int countInArray(int value, int[] array){
		int count = 0;
		for (int i = 0; i < array.length; i++){
			if (array[i] == value)
				count++;
		}
		
		return count;
	}//end countInArray
	
	//Load school information from a user-provided file and return the number of new schools.
	//New schools are added to the list of existing schools. 
	public static int loadSchools(ArrayList<School> H) throws IOException{
		String userInput = "initialized";
		int potentialSchools = 0, schoolsAdded = 0, currentSchools = H.size(); 
		boolean validFile = false;
		
		do{
			System.out.print("\nEnter school file name (0 to cancel): ");
			userInput = reader.readLine();
			
			if (userInput.equals("0")){
				System.out.println("\nFile loading process canceled.");
				return 0;
			}
			else {
				File file = new File(userInput);
				if ((!(file.exists())) || (!(file.isFile()))){// If the file name doesn't exist or is not a file
					System.out.println("\nERROR: File not found!");
					validFile = false;
				}
				else {
					validFile = true;
					BufferedReader fileReader = new BufferedReader(new FileReader(userInput));	
					String dataPoints = "initialized";
					
					do{
						dataPoints = fileReader.readLine();
						if (dataPoints!= null){
							potentialSchools++;
							String[] splitData = dataPoints.split(",");
							if (splitData.length == 3){ // check if valid number of arguments (3)
								//Create a temporary school object. Assume correct format. Parameters: name, alpha, maxMatches, numStudents
								School tempSchool = new School(splitData[0],Double.parseDouble(splitData[1]), Integer.parseInt(splitData[2]), H.size());
								
								if (tempSchool.isValid()){// If the school data passes all checks, add it to the school array
									H.add(currentSchools + schoolsAdded, tempSchool); //Add it to the next index in the schol array
									schoolsAdded++;
								}//end if
							}//end outer if
							
						}//end outer outer if		
						
					}while(dataPoints != null);
					fileReader.close(); //close file reader after we're done reading the file 
				}
			}
		}while(!validFile);
		
		if (schoolsAdded > 0 ){ //If any schools are added, then the students are erased therefore so are any previous assigned students and student rankings
			clearSchoolsStudentRankings(H, currentSchools + schoolsAdded); //clear school's rankings of students
			clearAssignedStudents(H); // clear previously matched students and clear regrets 
		}
		System.out.format("\n%d of %d schools loaded!\n", schoolsAdded, potentialSchools);
	
		return schoolsAdded;
	}//end loadSchools
	
	//Sub-area menu to edit students and schools
	public static void editData(ArrayList<Student> S, ArrayList<School> H) throws IOException{
		String userChoice = "initialized";
		
		while (userChoice.charAt(0) != 'Q'){
			userChoice = getOneLetterString("\nEnter choice: ", 'E');
			
			if (userChoice.charAt(0) == 'S')
					editStudents(S,H);
			else if (userChoice.charAt(0) == 'H')
					editSchools(S,H);
			else if (userChoice.charAt(0) == 'Q')
				System.out.println();
				}		
	}//end editData
	
	//Sub-area to edit students. The edited student's regret is updated if needed. 
	//Any existing school rankings and regrets are re-calculated after editing a student.
	public static void editStudents(ArrayList<Student> S, ArrayList<School> H) throws IOException{
		if (S.size() == 0)
			System.out.println("\nERROR: No students are loaded!");
		else {
				int studentToEdit = -2;
			do{
				System.out.println();
				printStudentTable(S, H, S.size());//Print table of students
				studentToEdit = getInteger("Enter student (0 to quit): ", 0, S.size());
			
			if (studentToEdit == 0)
				break;//break out of do-while if user inputs 0
			else {
				S.get(studentToEdit - 1).editInfo(H, true);
			     	// recalculate existing schools' rankings of students
				 schoolsRankStudents(S, H, H.size());
			}
			}while(studentToEdit != 0);
		}
	}//end editStudents
	
	//Sub-area to edit schools. Any existing rankings and regret for the edited school are updated.
	public static void editSchools(ArrayList<Student> S, ArrayList<School> H) throws IOException{
		if (H.size() == 0)
			System.out.println("\nERROR: No schools are loaded!");
		else{
			int schoolToEdit = -3;
			do {
				System.out.println();
				printSchoolTable(S, H, H.size());//Print table of schools
				schoolToEdit = getInteger("Enter school (0 to quit): ", 0, H.size());
				
				if (schoolToEdit == 0)
					break; 
				else H.get(schoolToEdit - 1).editSchoolInfo(S, true);
				
			}while(schoolToEdit != 0);
		}
	}//end editSchools
	
	//Print students to the screen, including matched school (if one exists).
	public static void printStudents(ArrayList<Student> S, ArrayList<School> H){
		if (S.size() == 0)//checking twice to be sure!
			System.out.println("\nERROR: No students are loaded!");
		else {
			System.out.println("\nSTUDENTS:\n");
			printStudentTable(S, H, S.size());
		}
	}//end printStudents
	
	public static void printStudentTable(ArrayList<Student> S, ArrayList<School> H, int numStudents){//print out just the students table and its values
		System.out.println(" #   Name                                         GPA  ES  Assigned school                         Preferred school order");
		System.out.println("---------------------------------------------------------------------------------------------------------------------------");
		for (int sc = 0; sc < numStudents; sc++){
			System.out.format("%3d. ", sc+1);
			S.get(sc).print(H);
		}
		System.out.println("---------------------------------------------------------------------------------------------------------------------------");

	}//end printStudentTable
	
	//Print schools to the screen, including matched student (if one exists).
	public static void printSchools(ArrayList<Student> S, ArrayList<School> H){
		if (H.size() == 0)
			System.out.println("\nERROR: No schools are loaded!\n");
		else{
				System.out.println("\nSCHOOLS:\n");	
				printSchoolTable(S,H, H.size());
				System.out.println();
		}
			
	}//end printSchools
	
	public static void printSchoolTable(ArrayList<Student> s, ArrayList<School> h, int numSchools){//Method prints out just the school table and its values 
		
		System.out.println(" #   Name                                     # spots  Weight  Assigned students                       Preferred student order");
		for (int i = 0; i < 126; i++){
			System.out.print("-");
		}
		System.out.println();
		for (int hc = 0; hc < numSchools; hc++){
			System.out.format("%3d. ", hc+1);;
			h.get(hc).print(s);
		}
		
		for (int i = 0; i < 126; i++){
			System.out.print("-");
		}
		System.out.println();
	}//end printSchoolTable

	//print comparison of student-optimal and school-optimal solutions
	public static void printComparison(SMPSolver GSS, SMPSolver GSH){ //GSS = Students are suitors; GSH = Schools are suitors 
		
		System.out.println("\nSolution              Stable    Avg school regret   Avg student regret     Avg total regret       Comp time (ms)");
		System.out.println("----------------------------------------------------------------------------------------------------------------");
	
		// print out student optimal i.e. GSS object stats 
		GSS.printStatsRow("Student optimal");
	
		// print out school optimal i.e. GSH object stats
		GSH.printStatsRow("School optimal");
		System.out.println("----------------------------------------------------------------------------------------------------------------");
		
		System.out.format("%-15s", "WINNER");
		//compare stabilities
		if ((GSS.isStable() && GSH.isStable()) || (!GSS.isStable() && !GSH.isStable())) // if both solutions are stable or both unstable 
			System.out.format("%13s", "Tie"); // it's a tie
		else if (GSS.isStable() && !GSH.isStable()) // if only student-optimal is stable
 			System.out.format("%13s", "Student-opt"); // 
		else System.out.format("%13s", "School-opt"); // if only school-optimal is stable 
		
		//compare average school regrets
		if (GSS.getAvgReceiverRegret() == GSH.getAvgSuitorRegret()) // if the regrets are equal
			System.out.format("%21s", "Tie");
		else if (GSS.getAvgReceiverRegret() < GSH.getAvgSuitorRegret()) // if the student-opt school regret is lower 
			System.out.format("%21s", "Student-opt");
		else System.out.format("%21s", "School-opt");
		
		//compare average student regrets
		if (GSS.getAvgSuitorRegret() == GSH.getAvgReceiverRegret()) // if the regrets are equal
			System.out.format("%21s", "Tie");
		else if (GSS.getAvgSuitorRegret() < GSH.getAvgReceiverRegret()) // if the student-opt student regret is lower 
			System.out.format("%21s", "Student-opt");
		else System.out.format("%21s", "School-opt");
		
		//compare average total regrets
		if (GSS.getAvgTotalRegret() == GSH.getAvgTotalRegret()) // if the total regrets are equal
			System.out.format("%21s", "Tie");
		else if (GSS.getAvgTotalRegret() < GSH.getAvgTotalRegret()) // if the student-opt total regret is lower 
			System.out.format("%21s", "Student-opt");
		else System.out.format("%21s", "School-opt");
		
		//compare computation times
		if (GSS.getTime() == GSH.getTime()) // if comp. times are equal
			System.out.format("%21s\n", "Tie");
		else if (GSS.getTime() < GSH.getTime()) // if the student-opt time is faster 
			System.out.format("%21s\n", "Student-opt");
		else System.out.format("%21s\n", "School-opt");
			
		System.out.println("----------------------------------------------------------------------------------------------------------------\n");
	
		
	}//end printComparison
	
	public static ArrayList<School> copySchools(ArrayList<School> P){
		ArrayList<School> newList = new ArrayList<School>();
		for (int i = 0; i < P.size(); i++){
			String name = P.get(i).getName();
			double alpha = P.get(i).getAlpha();
			int maxMatches = P.get(i).getMaxMatches();
			int nStudents = P.get(i).getNParticipants();
			School temp = new School(name, alpha, maxMatches, nStudents);
			for (int j = 0; j < nStudents; j++){
				temp.setRanking(j, P.get(i).getRanking(j));
			}
			newList.add(temp);
		}
		return newList;
	}
	
	public static ArrayList<Student> copyStudents(ArrayList<Student> P){
		ArrayList<Student> newList = new ArrayList<Student>();
		for (int i = 0; i < P.size(); i++){
			String name = P.get(i).getName();
			double GPA = P.get(i).getGPA();
			int ES = P.get(i).getES();
			int nSchools = P.get(i).getNParticipants();
			Student temp   = new Student(name, GPA, ES, nSchools);
			for (int j = 0; j < nSchools; j++){
				temp.setRanking(j, P.get(i).getRanking(j));
			}
			newList.add(temp);
		}
		return newList;
	}
	
	//Update the solvers to have the latest version of Student and School arrays
	public static void updateParticipants(SMPSolver GSS, SMPSolver GSH, ArrayList<Student> S, ArrayList<School> H) {
		
	if (S != null && H != null){
        // GSS solver duplicate arrays
        ArrayList<School> H1 = copySchools(H);
        ArrayList<Student> S1 = copyStudents(S);
        if (GSS.matchesExist())
        	copyAllMatches(GSS, S1, H1);
        GSS.setParticipants(S1, H1); // suitors first 

        // GSH solver duplicate arrays
        ArrayList<School> H2 = copySchools(H);
        ArrayList<Student> S2 = copyStudents(S);
        if (GSH.matchesExist())
        	copyAllMatches(GSH, H2, S2);
        GSH.setParticipants(H2, S2); // suitors second 
	}

}

// copy matches from one solver into S and R to maintain original matching
public static void copyAllMatches(SMPSolver GS, ArrayList<? extends Participant> S, ArrayList<? extends Participant> R) {
        copyMatches_oneGroup(GS.getSuitors(), S);
        copyMatches_oneGroup(GS.getReceivers(), R);
}

// copy participant matches in P into newP
public static void copyMatches_oneGroup(ArrayList<Participant> P, ArrayList<? extends Participant> newP) {
        for (int i = 0; i < P.size(); i++) {
                newP.get(i).clearMatches();
                for (int j = 0; j < P.get(i).getNMatches(); j++){
                        newP.get(i).setMatch(P.get(i).getMatch(j));
                        newP.get(i).setFreeStatus(false);
                }
        }
}


	//Prints specific integer interval statement depending on LB and UB values
	public static void printIntegerInterval(int LB, int UB){ 
		if (LB <= -(Integer.MAX_VALUE) && UB >= Integer.MAX_VALUE)//if both values are larger than the max storable integer values 
			System.out.println("[-infinity, infinity]!\n");
		else if (LB <= -(Integer.MAX_VALUE))//if the lower bound is smaller than the smallest storable integer value
			System.out.println("[-infinity, " + UB+ "]!\n");
		else if (UB >= Integer.MAX_VALUE)// if the upper bound is bigger than the largest storable integer value
			System.out.println("[" + LB + ", infinity]!\n");
		else
			System.out.println("[" + LB + ", " + UB + "]!\n");//if both integers are smaller than the max storable values
	}//end printIntegerInterval
	
	//Prints specific double interval depending on LB and UB Values
	public static void printDoubleInterval(double LB, double UB){
		if (LB <= -(Double.MAX_VALUE) && UB >= Double.MAX_VALUE)
			System.out.println("[-infinity, infinity]!\n");
		else if (LB <= -(Double.MAX_VALUE))
			System.out.format("[-infinity, %.2f]!\n\n", UB);
		else if (UB >= Double.MAX_VALUE)
			System.out.format("[%.2f, infinity]!\n\n", LB);
		else 
			System.out.format("[%.2f, %.2f]!\n\n", LB, UB);//if both doubles are smaller than the max storable values
	}//end printDoubleInterval
	
	//Function that gets an integer value from user
	public static int getInteger(String prompt, int LB, int UB){
		boolean validNum = true, exceptionThrown = false;
		int userInput = 200;//initialize user input variable
		do{
		System.out.print(prompt);//prompt user
		try{//attempt to read user input
			userInput = Integer.parseInt(reader.readLine());	 
		}catch(IOException e){
			validNum = false;
			exceptionThrown = true;//exception has been thrown
			
		}catch(NumberFormatException e){
			validNum = false;
			exceptionThrown = true;
			
		}
		if (userInput >= LB && userInput <= UB && !exceptionThrown){//If the user input is within the range of valid options
			validNum = true;
			exceptionThrown = false;
		}
		else{
			validNum = false;
			}
		if (!validNum){
			System.out.print("\nERROR: Input must be an integer in ");
			printIntegerInterval(LB, UB);
		}
		exceptionThrown = false;//Set exceptionThrown back to false once loop ends
		}while(!validNum);
		
		return userInput;
	}//end getInteger
	
	//Function that gets a double value from the user
	public static double getDouble(String prompt, double LB, double UB){
		boolean validDouble = true, exceptionThrown = false;
		double userInput = -10.0;//initializing variables
		do{
			System.out.print(prompt);
			try{//attempt to read user input
				userInput = Double.parseDouble(reader.readLine());	 
			}catch(IOException e){//Do the same thing for both exceptions that could be thrown 
				validDouble = false;
				exceptionThrown = true;//exception has been thrown
			}catch(NumberFormatException e){
				validDouble = false;
				exceptionThrown = true;
			}	
			if (userInput >= LB && userInput <= UB && !exceptionThrown){//If the number is a valid number
				validDouble = true;
				exceptionThrown = false;
			}
			else{
				validDouble = false;
			}
			if (!validDouble){
				System.out.print("\nERROR: Input must be a real number in ");		
				printDoubleInterval(LB, UB);	
			}	
			exceptionThrown = false;
		} while(!validDouble);
		return userInput;
	}//end getDouble
	
	public static String getOneLetterString(String prompt, char menuType){
		boolean validLetter = true;
		String userInput = "initialized"; 
		do{		
			if (menuType == 'D')//Use menuType to choose which menu  or sub-menu we want to display
			displayMenu();
			else if (menuType == 'E'){
				System.out.println();//For correct formating
				displayEditMenu();
			}
			
			System.out.print(prompt);
			try{
				userInput = reader.readLine();
			}catch(IOException e){//Catch the general IOException
				validLetter = false;
			}catch(StringIndexOutOfBoundsException e){
				  validLetter = false;
			  }
			
			
			if (userInput.length() != 1){// If user enters a string longer/shorter than one character 
				validLetter = false;	// We already know thats invalid
			}
			else if (userInput.length() == 1){
				userInput = userInput.toUpperCase();//turn user input into all capitals
					
					validLetter = isValidMenuChoice(userInput, menuType);//Valid letter is only true in this case
			}
			
			if (!validLetter){//Print out error message if valid letter is false
				if (menuType == 'D')
					System.out.println("\nERROR: Invalid menu choice!\n");
				else if (menuType == 'E')//fixed spacing error
					System.out.println("\nERROR: Invalid menu choice!");
				else if (menuType == 'R')
					System.out.println("ERROR: Choice must be 'y' or 'n'!");
			}
		}while(!validLetter);
		
		return userInput;
	}//end getString
	
	public static boolean isValidMenuChoice(String userInput, char menuType){
		
		if (menuType == 'D'){//Checking valid options for main menu
			if (userInput.charAt(0) == 'L' || userInput.charAt(0) == 'E' ||//If the user input is a valid menu option
					userInput.charAt(0) == 'P' || userInput.charAt(0) == 'M' || 
					userInput.charAt(0) == 'D' || userInput.charAt(0) == 'X' ||
					userInput.charAt(0) == 'R' || userInput.charAt(0) == 'Q')
				return true;//return true
			else return false;
			}
		else if (menuType == 'E'){//checking valid options for edit sub-menu
			if (userInput.charAt(0) == 'S' || userInput.charAt(0) == 'H' || userInput.charAt(0) == 'Q')
				return true;
			else return false;
		}
		else if (menuType == 'R'){//checking valid options for choice to edit rankings or not to
			if (userInput.charAt(0) == 'Y' || userInput.charAt(0) == 'N')
				return true;
			else return false;
		}
		return false;
		}//end isValidChoice
	
	public static double[] sortDescending(double[] array){//Function that sorts the values in a double-type array in descending order
        double temp;
        for(int i=0; i < array.length-1; i++){
            for(int j=1; j < array.length-i; j++){
                if(array[j-1] < array[j]){//compare if number to the left is smaller than number to the right
                    temp=array[j-1];
                    array[j-1] = array[j];
                    array[j] = temp;
                }
            }
        }
        return array;
    }//end sortDescending
	
	public static void main(String[] args) throws IOException{
		char userChoice;
		boolean running = true;
		ArrayList<Student> studentArray = new ArrayList<Student>();
		ArrayList<School> schoolArray = new ArrayList<School>();
		SMPSolver studentOpt = new SMPSolver(); // student-optimal SMP Solver 
		SMPSolver schoolOpt = new SMPSolver();// school-optimal SMP Solver
		schoolOpt.setSuitorFirst(false);
 		
		do{
			userChoice = getOneLetterString("Enter choice: ", 'D').charAt(0);
			
			switch(userChoice){
			case 'L':
				if(loadParticipants(studentArray, schoolArray)){//If any new participants have been added
					studentOpt.setMatchesExist(false); // any previous matches are invalid now
					schoolOpt.setMatchesExist(false);
					updateParticipants(studentOpt, schoolOpt, studentArray, schoolArray); //update the participants 
				}
				break;
			case 'E':
				if (studentOpt.matchesExist() && schoolOpt.matchesExist()) // if matches exist for both solutions 
					copyAllMatches(studentOpt, studentArray, schoolArray); // make sure the assigned studentArray and SchoolArray are updated with these
																			// before the edit menu is opened
				editData(studentArray, schoolArray); //Edit the participants
				updateParticipants(studentOpt, schoolOpt, studentArray, schoolArray); //update the participants in case any changes have been made				
				break;
			case 'P': // We print the student-optimal results
				if (!studentOpt.matchesExist()){ // Make sure any assigned students are cleared if matches haven't been made yet
					clearAssignedStudents(schoolArray);
					clearAssignedSchools(studentArray);
				}
				else if (studentOpt.matchesExist() && schoolOpt.matchesExist()) // else if matches exist for both solutions 
					copyAllMatches(studentOpt, studentArray, schoolArray);
		
				printStudents(studentArray, schoolArray);
				printSchools(studentArray, schoolArray);
				break;
			case 'M':
				System.out.println("\nSTUDENT-OPTIMAL MATCHING");
					if (studentOpt.matchingCanProceed()) //if matching can proceed for the student-opt
						studentOpt.match(); // execute the Gale-Shapley algorithm
					System.out.println("SCHOOL-OPTIMAL MATCHING");
					if (schoolOpt.matchingCanProceed())
						schoolOpt.match();					
				break;
			case 'D':
				System.out.println("\nSTUDENT-OPTIMAL SOLUTION");
				if(!studentOpt.matchesExist())
					System.out.println("\nERROR: No matches exist!\n");
				else studentOpt.print();
			
				System.out.println("SCHOOL-OPTIMAL SOLUTION");
				if(!schoolOpt.matchesExist())
					System.out.println("\nERROR: No matches exist!\n");
				else schoolOpt.print();
				
				break;
			case 'X':
				if (!studentOpt.matchesExist() && !schoolOpt.matchesExist())//if no matches exist
					System.out.println("\nERROR: No matches exist!\n");
				else 
					printComparison(studentOpt, schoolOpt);
				break;
			case 'R':
				clearDatabases(schoolArray, studentArray);
				studentOpt.reset();
				schoolOpt.reset();
				break;
			case 'Q':
				running = false;
				break;
			}
		}while (running);
		
		System.out.println("\nHasta luego!");
		
	}
}
