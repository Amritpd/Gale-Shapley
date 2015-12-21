import java.io.IOException;
import java.util.ArrayList;

public class School extends Participant{
	private double alpha;   // GPA weight
	private double[] compositeScores; // Array of composite scores
	
	// Constructors
	public School(){//Initialize values to be sure 
		super();
		this.setAlpha(0);
	}
	public School(String name, double alpha, int maxMatches, int numStudents){
		super(name, maxMatches, numStudents);
		this.setAlpha(alpha);
	}
	
	//Getter functions
	public double getAlpha(){return this.alpha;}
	public double getCompositeScore(int i){return this.compositeScores[i];}
		
	//setter functions
	public void setAlpha(double alpha){this.alpha = alpha;}
	public void setCompositeScore(int i, double c){this.compositeScores[i] = c;}
	
	@Override
	public void setNParticipants(int n){ //Set rankings array size
		super.setNParticipants(n);
		this.compositeScores = new double[n];//both arrays are of the same size
	}
	
	//get new info from the user
	public void editSchoolInfo(ArrayList<Student> S, boolean canEditRankings) throws IOException{
		
		System.out.print("\nName: ");
		this.setName(GaleShapleyV1.reader.readLine());// read in name
		this.setAlpha(GaleShapleyV1.getDouble("GPA weight: ",0., 1.));//set the GPA weight
		super.editInfo(S);
		if(canEditRankings){//If previous rankings exist
			this.resetRankings();
			this.calcRankings(S);// Recalculate/update the rankings since alpha could have changed
			
			if (!this.isFree())//Implies: "If matches have been made previously"
				this.calcRegret();// Recalculate the regret
		}
	}
	
	//calculate rankings based on weight alpha
	public void calcRankings(ArrayList<Student> S){
		
		for (int sc = 0; sc < S.size(); sc++){
				double compositeScore = (this.getAlpha()*S.get(sc).getGPA()) + ((1 - this.getAlpha())*S.get(sc).getES());//Calculate composite score of student
				this.setCompositeScore(sc, compositeScore);//Add to array of composite scores corresponding to the appropriate student	
		}
		
		double[] sortedScores = new double[this.compositeScores.length];//creating sorted version of composite scores
		for (int i = 0; i < sortedScores.length; i++){//copy values from composite Scores array to this one
			sortedScores[i] = this.getCompositeScore(i);
		}
		sortedScores = GaleShapleyV1.sortDescending(sortedScores);//sort the composite scores in descending order
		
		for (int c = 0; c < sortedScores.length; c++){//iterate through the sortedScores array
			double compositeScore = sortedScores[c];//get the next highest score from the sorted array
				for (int j = 0; j < this.compositeScores.length; j++){
					if ((this.getCompositeScore(j) == compositeScore) && (this.getRanking(j) == 0)){//check == 0 to ensure a ranking has not already been assigned 					
						this.setRanking(j, (c+1));//set the ranking of the j'th student
						break;//break once we have assigned a ranking because there is no point to continue iterating
					}
				}
		}
	}
	
	//print school info and assigned student in tabular format
	@Override
	public void print(ArrayList<? extends Participant> S){
		System.out.format("%-41s", this.getName());//print school name
		System.out.format("%7d", this.getMaxMatches());// print # spots
		System.out.format("    %.2f", this.getAlpha());//print GPA weight, alpha
		
		if (this.isFree()) //print dash if students have not been assigned to this school 
			System.out.print("  -                                       ");
		else {
			super.print(S);
		}//print the assigned student names 
		
		if (this.getRankingsLength() != 0 )
			super.printRankings(S);
		else{
			System.out.println("-");
		}
	}
		
	@Override
	public boolean isValid(){ // check if this school has valid info
		if (super.isValid()) // valid max number of matches 
			if (this.alpha >= 0 && this.alpha <= 1) // Valid GPA weight 
				return true;
		
		return false;
	}
}

