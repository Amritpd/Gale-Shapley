import java.io.IOException;
import java.util.ArrayList;

public class Participant {
	private String name; // name
	private int[] rankings; // rankings of participants
	private ArrayList<Integer> matches = new ArrayList<Integer>(); // matches indices
	private int regret; // total regret
	private int maxMatches; // max # of allowed matches/openings 
	private boolean free = true; //  free status of participant; becomes false when maxMatches = size of matches array
	private int nextRankToProposeTo = 1; // holds rank value that's used to find the index of the next school to propose to

	//constructor
	public Participant(){
		this.setName(null);
		this.setNParticipants(0);
		this.setRegret(-1);
		this.setMaxMatches(0);
	}
	public Participant(String name, int maxMatches, int numParticipants){
		this.setName(name);
		this.setNParticipants(numParticipants);
		this.setRegret(-1);
		this.setMaxMatches(maxMatches);
	}
	
	//getters
	public String getName(){return this.name;}
	public int getRanking(int i){return this.rankings[i];}
	public int getMatch(int i){return this.matches.get(i);}
	public int getRegret(){return this.regret;}
	public int getMaxMatches(){return this.maxMatches;}
	public int getNMatches(){return this.matches.size();}
	public int getNParticipants(){ return this.rankings.length;}

	public boolean isFull(){
		if (this.matches.size() == this.maxMatches){ // If the size of the matches array = the max matches 
			return true; // make sure free is set to false now 	
		}
		else return false; 
	}
	public boolean isFree() {return free;}
	public int getNextRankToProposeTo() {return nextRankToProposeTo;}
	public ArrayList<Integer> getMatches() {return this.matches;}
	public int getRankingsLength(){return this.rankings.length;}
	
	//setters
	public void setName(String name){this.name = name;}
	public void setRanking(int i, int r){this.rankings[i] = r;}//i is index, r is ranking 
	public void setMatch(int m){
		this.matches.add(m);
		if (this.isFull()) // If this participant is full after adding m
			this.setFreeStatus(false); // It's no longer  free 
	} // add the index of the match 
	public void setRegret(int r){this.regret = r;}
	public void setNParticipants(int n){
		this.rankings = new int[n];
		this.resetRankings();}
	public void setMaxMatches(int n){this.maxMatches = n;}
	public void setFreeStatus(boolean free) {this.free = free;}
	public void setNextRankToProposeTo(int nextRankToProposeTo) {this.nextRankToProposeTo = nextRankToProposeTo;}
	
	public void resetRankings(){//initialize rankings values with 0
		for (int i = 0; i < this.rankings.length; i++)
				this.rankings[i] = 0;	
	}
	public void setMatches(ArrayList<Integer> matches) {this.matches = matches;}
	
	//method to handle matches
	public void clearMatches(){ // clear all matches
		this.matches.clear();	
	}
	public int findRankingByID(int k){ // find rank of participant k
		return this.rankings[k];
	}
	public int getWorstMatch(){ // find the worst-matched participant aka participant that is ranked the lowest in the matches array
		int worstMatch = -1, worstMatchIndex = -1;
		
		for (int mc = 0; mc < this.matches.size(); mc++){ // iterate through the matches array and get index of match
			int matchRank = this.rankings[this.matches.get(mc)]; // get the rank of that match
			//	System.out.println("Match rank: " + matchRank);
			if (matchRank > worstMatch){ // if the match's rank is worse than the current worst match rank
				worstMatch = matchRank; // this is the new worst match rank
				worstMatchIndex = mc; // keep track of the participant index with the worst rank
				
			}
				
		}
	//	System.out.println("Worst Match in the matches array is at index: " + worstMatchIndex + " || Which means worst match in participant array is: " + this.getMatch(worstMatchIndex));
		return this.getMatch(worstMatchIndex); //return the index  in the participant array of the lowest ranked match
	}
	
	public void unmatch(int k){ // remove the match with participant k
		this.matches.set(k, -1); // by giving it a negative index
	}
	public boolean matchExists(int k){ // check if match to participant k exists
		if (this.matches.contains(k)) // if the participant index is in the matches array
			return true; // return true 
		else return false;
	}

	public int getSingleMatchedRegret(int k){ // get regret from match with k. K is the position in the matches array
		return this.rankings[this.matches.get(k)] - 1; // Regret is one less than the ranking of k
	}
	public void calcRegret(){ // calculate total regret over all matches
		this.regret = 0; // reset the regret 
		
		for (int mc = 0; mc < this.matches.size(); mc++){ // instead of using maxMatches we could uses matches array size. mc is the current position in the matches array
			this.regret += this.getSingleMatchedRegret(mc); // regret is one less than the kth participant's ranking
		}
	}
	
	// method to edit data from the user 
	public void editInfo(ArrayList<? extends Participant> P ) throws IOException{
		this.setMaxMatches(GaleShapleyV1.getInteger("Maximum number of matches: ", 1, Integer.MAX_VALUE));
	}
	public void editRankings(ArrayList<? extends Participant> P){
			char userChoice = 'I';
				
			userChoice = GaleShapleyV1.getOneLetterString("Edit rankings (y/n): ", 'R').charAt(0);
			
			if (userChoice == 'Y'){
				System.out.println("\n" + "Participant " + this.getName() + "'s rankings:");
				
					this.resetRankings();// erase the previous rankings and start fresh 
				
				for (int hc = 0; hc < P.size(); hc++){//hc = participant counter
					if (P.get(hc) != null){//Safety check
						boolean validRanking = false;
						int rank;
						do{
							 rank = GaleShapleyV1.getInteger("School " + P.get(hc).getName() + ": ", 1, this.rankings.length);
							 validRanking = isUniqueRank(rank);//check if unique rank is given
						}while(!validRanking);
						
						if(validRanking){//
							this.setRanking(hc, rank);//participants stay in same ordered list as participant array														
						}								   
						//amount of rankings will equal the amount of schools	
					}
				}//end for loop
				
				//If either this object's regret or participant index values are not -1, then matches have been made at one point
				if (this.getRegret() != -1 && !this.matches.contains(-1))// Implies "If matches have been made previously"
					this.calcRegret();
			
			System.out.println();
			}//end outer if 
	}
	
	public boolean isUniqueRank(int rank){//Method checks if a rank has already been entered by a user
		for (int counter = 0; counter < this.rankings.length; counter++){
			if (this.getRanking(counter) == rank){
				System.out.println("ERROR: Rank " + rank + " already used!\n");	
				return false;
			}		
		}
		return true;	
	}
	
	public void print(ArrayList<? extends Participant> P){ // only things common in printing is printing assigned matches and rankings 
		System.out.format("  %-40s", this.getMatchNames(P)); // print out the assigned matches
	}
	public void printRankings(ArrayList<? extends Participant> P ){
		for (int rank = 1; rank <= this.rankings.length; rank++){ // Start at rank 1, iterate all the way through to the last rank
			for (int pc = 0; pc < this.rankings.length; pc++){ //
				if (rank == this.getRanking(pc)){//If the rank matches the rank at the index, match found
					System.out.print(P.get(pc).getName());//Print out participant name
				if (rank != this.rankings.length)//If we're not at the last participant to print
					System.out.print(", ");//print a comma and space
				else System.out.println();//otherwise move on to the next line
				}
			}
		}
	}
	public String getMatchNames(ArrayList<? extends Participant> P){ // Names printed in order of highest ranked to lowest ranked 
		String matchNames = "";
		
		for (int mc = 0; mc < this.matches.size(); mc++){ //iterate through the matches
			matchNames += P.get(this.matches.get(mc)).getName(); // add the name of the match to the string
			if (mc != this.matches.size() - 1)
				matchNames += ", ";
		}
		return matchNames; 
	}
	
	//check if this participant has valid info
	public boolean isValid(){ 
		if (this.maxMatches < 1) // if the max matches is less than one 
			return false; // this is false 
		else return true;
	}
	
}
