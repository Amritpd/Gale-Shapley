import java.util.ArrayList;

public class SMPSolver { // Class runs the Gale-Shapley algorithm and stores matching results

	private ArrayList<Participant> S = new ArrayList<Participant>(); // suitors 
	private ArrayList<Participant> R = new ArrayList<Participant>();   // receivers 
	private double avgSuitorRegret;	// average suitor regret
	private double avgReceiverRegret;  // average receiver regret
	private double avgTotalRegret;     // average total regret
	private boolean matchesExist;   // whether or not matches exist
	private boolean stable; // whether or not matching is stable
	private long compTime; // computation time
	private boolean suitorFirst; // whether to print suitor stats first
	
	
	// constructor
	public SMPSolver(){
		this.setMatchesExist(false);
		this.avgReceiverRegret = this.avgSuitorRegret = this.avgTotalRegret = -1;
		this.suitorFirst = true; 
		this.stable = false;
	}
		
	public void reset(){ 
		this.S = this.R = null;
		this.setMatchesExist(false);
		this.avgReceiverRegret = this.avgSuitorRegret = this.avgTotalRegret = -1;
		this.stable = false; 
	}
	
	
	//getters
	public double getAvgSuitorRegret(){return this.avgSuitorRegret;}
	public double getAvgReceiverRegret(){return this.avgReceiverRegret;}
	public double getAvgTotalRegret(){return this.avgTotalRegret;}
	public boolean matchesExist(){return this.matchesExist;}
	public boolean isStable(){return this.stable;}
	public long getTime(){return this.compTime;}
	public int getNSuitorOpenings(){ // the number of openings = the number of max matches
		int numOpenings = 0;
			for (int sc = 0; sc < this.S.size(); sc++){
				numOpenings += S.get(sc).getMaxMatches();
			}
		return numOpenings; 
	}
	public int getNReceiverOpenings(){
		int numOpenings = 0;
		for (int rc = 0; rc < this.R.size(); rc++){
			numOpenings += R.get(rc).getMaxMatches();
		}
	return numOpenings; 
		}
	
	public ArrayList<Participant> getSuitors() {
		// TODO Auto-generated method stub
		return this.S;
	}

	public ArrayList<Participant> getReceivers() {
		// TODO Auto-generated method stub
		return this.R;
	}
	
	//setters
	public void setMatchesExist(boolean b){this.matchesExist = b;}
	public void setSuitorFirst(boolean b){this.suitorFirst = b;}
	@SuppressWarnings("unchecked")
	public void setParticipants(ArrayList<? extends Participant> S, ArrayList<? extends Participant> R){
		 // Hopefully casting will keep the values that we need 
		
			this.S = (ArrayList<Participant>) S; //S is suitors 
			this.R = (ArrayList<Participant>) R; //R is receivers 
	}
	
	// clear out existing matches
	public void clearMatches(){
		GaleShapleyV1.clearAssignedParticipants(S); // clear any previous matches/assignments/regrets made in the suitor
		GaleShapleyV1.clearAssignedParticipants(R); // array and the receiver array
	}
	
	public boolean matchingCanProceed(){// check that matching rules are satisfied
		boolean matchingCanProceed = false;

		if( this.S == null || this.S.size() == 0)
			System.out.println("\nERROR: No suitors are loaded!\n");
		else if (this.R == null || this.R.size() == 0)
			System.out.println("\nERROR: No receivers are loaded!\n");
		else if (getNSuitorOpenings() != getNReceiverOpenings())
			System.out.println("\nERROR: The number of suitor and receiver openings must be equal!\n");
		else matchingCanProceed = true;
		
		return matchingCanProceed;
	}
	
	// methods for matching
	public boolean match(){// Gale-Shapley algorithm to match; students are suitors
		
		this.clearMatches(); // Make sure we are matching the latest versions of the student/school array
		
		this.matchesExist = false;
		int numStudents = this.S.size(); //numStudents = numSchools

		long start = System.currentTimeMillis();// start of  Gale-Shapley algorithm
			do{
			this.matchesExist = true; // Assume that matches are made
				
				for (int sc = 0 ;  sc < numStudents; sc++){//Iterate through each suitor. Suitor proposes at most once in an iteration.
					
					if (this.S.get(sc).isFree()){// if the suitor is free
						this.matchesExist = false; // matching is not complete
						int nextParticipantToProposeTo = GaleShapleyV1.receiverIndexOfRank(this.S.get(sc), this.S.get(sc).getNextRankToProposeTo());//Find the next receiver the student has not proposed to yet
						
						if (this.makeProposal(sc, nextParticipantToProposeTo)) // make a proposal to the receiver and if they accept
							this.makeEngagement(sc, nextParticipantToProposeTo); // match them. Deal with potential consequences
						else this.S.get(sc).setNextRankToProposeTo(this.S.get(sc).getNextRankToProposeTo() + 1); // make sure the suitor proposes to its next preference on the next iteration
						}
					}
			}while(!this.matchesExist); // Algorithm terminates
		
			this.compTime = System.currentTimeMillis() - start; //calculate elapsed time algorithm ran for		
			
		this.printStats();//print matching statistics 
		System.out.format("%d matches made in %dms!\n\n", this.getNSuitorOpenings(), this.compTime);
			
		return this.matchesExist;
	}//end match
	
	private boolean makeProposal(int suitor, int receiver){// suitor proposes
			boolean acceptProposal = false;
			if (!this.R.get(receiver).isFull()) // If the receiver is not full then automatically engage
				acceptProposal = true;
			// if the receiver prefers the current suitor over the worst suitor its matched with
			else if (this.R.get(receiver).getRanking(this.R.get(receiver).getWorstMatch()) > this.R.get(receiver).getRanking(suitor)) 
				acceptProposal = true;

		return acceptProposal;
	}//end makeProposal
	
	private void makeEngagement(int suitor, int receiver) {// make engagement with suitor and receiver
	
			if (this.S.get(suitor).isFull() || this.S.get(suitor).matchExists(-1)){ // If the suitor was previously full
				this.S.get(suitor).getMatches().set(this.S.get(suitor).getMatches().indexOf(-1), receiver); // 
				this.S.get(suitor).setFreeStatus(false);
			}
			else if (!this.S.get(suitor).isFull()){ // If the suitor isn't full				
				this.S.get(suitor).setMatch(receiver);// Add the receiver to its matches array 
			
			}
			
			if (!this.R.get(receiver).isFull()){// if the receiver isn't full 
				this.R.get(receiver).setMatch(suitor);// Add the proposing suitor to its matches array
				}
			else if (this.R.get(receiver).isFull()){ // but if the receiver is full we need to get its worst match, and the index of it in the participant array
				int worstMatchIndex = this.R.get(receiver).getWorstMatch(); // index of the worst match 
			//	System.out.println("Worst Match for Receiver " + receiver + " is " + worstMatchIndex); 
				this.S.get(worstMatchIndex).getMatches().remove(this.S.get(worstMatchIndex).getMatches().indexOf(receiver)); // remove the receiver from the old suitor 
				this.S.get(worstMatchIndex).setFreeStatus(true); // make sure that old suitor is free 
				
				this.R.get(receiver).getMatches().set(this.R.get(receiver).getMatches().indexOf(worstMatchIndex), suitor); // replace the old suitor with the new suitor in the receiver's matche array 
			}
		
	
	}//end makeEngagement
	
	public void calcRegrets(){// calculate regrets
		this.avgSuitorRegret = calcAvgParticipantRegret(this.S); // calculate average suitor regret
 		this.avgReceiverRegret = calcAvgParticipantRegret(this.R); // calculate average receiver regret
		this.avgTotalRegret = calcAvgTotalRegret(this.S, this.R); // calculate average total regret 
	}
	
	public double calcAvgParticipantRegret(ArrayList<Participant> P){
		double totalRegret = 0;
		
		for (int pc = 0; pc < P.size(); pc++){ // iterate through the participants, regardless if they are suitors/receivers 
			P.get(pc).calcRegret(); // calculate the regret (in case it wasn't done before)
			totalRegret += (double) P.get(pc).getRegret();// add it to the total regret 
		}
		
		return totalRegret/P.size();
	}
	
	public double calcAvgTotalRegret(ArrayList<Participant> S, ArrayList<Participant> R){
		double totalRegret = 0;
		
		for (int sc = 0; sc < S.size(); sc++){ // iterate through the participants, regardless if they are suitors/receivers 
			S.get(sc).calcRegret(); // calculate the regret (in case it wasn't done before)
			totalRegret += S.get(sc).getRegret();// add it to the total regret 
		}
		
		for (int rc = 0; rc < R.size(); rc++){// same idea except for receivers 
			R.get(rc).calcRegret();
			totalRegret += R.get(rc).getRegret();
		}
		
		return totalRegret/(S.size() + R.size());
	}
		
	public void stableSolution(){ // Helper function to call determineStability and assign value to boolean stable 
		if (determineStability())
			this.stable = true;
		else this.stable = false;
			
	}//end stableSolution
	
	public boolean determineStability(){ // Calculate if a matching is stable 
		for (int sc = 0; sc < S.size(); sc++){ //iterate through all the suitors 
			Participant currentSuitor = S.get(sc); // create a temporary reference to the sc'th suitor
			
			if (currentSuitor.getRegret() != 0){ // If the currentSuitor has some regret 
				for (int rank = currentSuitor.getRankingsLength(); rank >= 1; rank--){ //Start 
					int receiverIndex = GaleShapleyV1.receiverIndexOfRank(currentSuitor, rank); // get the receiver index of that rank in the participant array 
				
					if(!currentSuitor.matchExists(receiverIndex)){ // Only concerned with receivers that the suitor ISN'T matched with
						Participant currentReceiver = R.get(receiverIndex); // create a temporary reference to this receiver
						
							if (currentReceiver.getRegret() != 0){
								// If the receiver prefers the current suitor over its worst match AND the current suitor prefers the current receiver over its worst match
						if (currentReceiver.getRanking(currentReceiver.getWorstMatch()) > currentReceiver.getRanking(sc) && (currentSuitor.getRanking(currentSuitor.getWorstMatch()) > currentSuitor.getRanking(receiverIndex))){ 
							return false; // return false
						}
						}
					}
				}
			}
			
		}
		return true;
	}//End determineStability
	
	//print methods
	public void print(){ // print the matching results and statistics
		this.printMatches();
		this.printStats();
	}
	public void printMatches(){// print matches
		System.out.print("\nMatches:\n--------\n");
		
		if (this.suitorFirst){
			for(int hc = 0; hc < this.R.size(); hc++){
				System.out.println(this.R.get(hc).getName() + ": " + this.R.get(hc).getMatchNames(S));
				
			} //Print name of the high school, and print the name of the student that the school's matched with
		}
		else if (!this.suitorFirst){
			for (int sc = 0; sc < this.S.size(); sc++){
				System.out.println(this.S.get(sc).getName() + ": " + this.S.get(sc).getMatchNames(R));
			}
		}
	}
	public void printStats(){// print matching statistics
		
		System.out.print("\nStable matching? ");
		
		this.calcRegrets();
		// A matching is stable because no suitor or receiver would rather be with someone who would also rather be with them. The best possible solution
		//is the stable solution with the least regret
		this.stableSolution(); //determine the stability of the solution
		if (this.isStable())
			System.out.println("Yes");
		else System.out.println("No");
		
		//calculate and print regrets
		
		System.out.format("Average suitor regret: %.2f\n", this.avgSuitorRegret);
		System.out.format("Average receiver regret: %.2f\n",this.avgReceiverRegret);
		System.out.format("Average total regret: %.2f\n", this.avgTotalRegret);
		System.out.println();
	}
	
	public void printStatsRow(String rowHeading){ // print statistics as row 
		String output = "Initialized";
		this.calcRegrets();
		this.stableSolution();
		System.out.format("%-15s", rowHeading); //Print out the row heading
		if (this.isStable()) // If the solution is stable
			output = "Yes"; // output is yes else it's no
		else output = "No";
		
		if (this.suitorFirst) // If the suitor is first - Student-Opt Solver
			System.out.format("%13s%21.2f%21.2f%21.2f%21d\n", output, this.getAvgReceiverRegret(), this.getAvgSuitorRegret(), this.getAvgTotalRegret(), this.getTime());
		else if (!this.suitorFirst) // If the suitor is second - School-Opt Solver 
			System.out.format("%13s%21.2f%21.2f%21.2f%21d\n", output, this.getAvgSuitorRegret(), this.getAvgReceiverRegret(), this.getAvgTotalRegret(), this.getTime());

	}

}
