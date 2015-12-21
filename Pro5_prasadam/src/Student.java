import java.io.IOException;
import java.util.ArrayList;

public class Student extends Participant{
	private double GPA;     // Student's GPA, ranges from 0.00 - 4.00
	private int ES;         // extracurricular score, ranges from 0-5
	
	
	//constructors 
	public Student(){
		super();
		this.setGPA(0);
		this.setES(0);
	}
	
	public Student(String name, double GPA, int ES, int nSchools){
		super(name, 1, nSchools);
		this.setGPA(GPA);
		this.setES(ES);
	}

	//getter functions	
	public double getGPA(){return this.GPA;}
	public int getES(){return this.ES;}
	
	//setter functions
	public void setGPA(double GPA){this.GPA = GPA;}
	public void setES(int ES){this.ES = ES;}
	
	public void editInfo(ArrayList<School> H, boolean canEditRankings) throws IOException{
		System.out.print("\nName: ");
		this.setName(GaleShapleyV1.reader.readLine());// read in name
		this.setGPA(GaleShapleyV1.getDouble("GPA: ", 0., 4.));// read in GPA
		this.setES(GaleShapleyV1.getInteger("Extracurricular score: ", 0, 5)); // read in ES
		super.editInfo(H);
		if (canEditRankings)
			super.editRankings(H);
	}	
	
	//print student info and assigned school in tabular format
	@Override
	public void print(ArrayList<? extends Participant> H){
			System.out.format("%-39s", this.getName());
			System.out.format("     %.2f", this.getGPA());
			System.out.format("%4d",this.getES());
			
			if (this.isFree()) // If no matches have been assigned previously
				System.out.print("  -                                       ");
			else {
				super.print(H);
			}
			if (this.getRanking(0) != 0){ // If rankings have been assigned
				super.printRankings(H);
			}
			else System.out.println("-");
		
	}
	
	@Override
	public boolean isValid(){// check if the student has valid info
	if (super.isValid()) // Check if the student has a valid max matches
	if (this.GPA >= 0. && this.GPA <= 4.) //Check if the student has a valid GPA
		if (this.ES >= 0 && this.ES <= 5) //Check if the student has a valid ES 
				return true;
	
		return false;
	}
	
	
}//end Student class 







