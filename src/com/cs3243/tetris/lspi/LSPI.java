package com.cs3243.tetris.lspi;

import java.util.Arrays;
import java.util.Random;

import com.cs3243.tetris.NextState;
import com.cs3243.tetris.PlayerSkeleton;
import com.cs3243.tetris.State;
import com.cs3243.tetris.features.Feature;
import com.cs3243.tetris.heuristics.Heuristic;

public class LSPI {
	public Sample[] samples;
	public Policy policy;
	public Matrix samplesMatrix;
	
	LSPI(Sample[] s, Policy p) {
		samples = s;
		policy = p;
	}

	LSPI(int numberOfRandomSamples) {
		generateSamples(numberOfRandomSamples);

		Heuristic heuristic = new Heuristic();
		policy = new Policy(heuristic);

		double[][] samplesArray = new double[samples.length][];

		for (int i = 0; i < samples.length; i++) {
			Sample sample = samples[i];
			NextState nsSample = new NextState();
			nsSample.copyState(sample.state);

			samplesArray[i] = policy.getFeatureScores(nsSample);
		}
		
		samplesMatrix = new Matrix(samplesArray);
	}
	
	public void nextIteration() {
		double[][] policyArray = new double[samples.length][];
		double[][] rewardsArray = new double[samples.length][1];

		for (int i = 0; i < samples.length; i++) {
			Sample sample = samples[i];
			
			// Create a new state fron nextState
			
			NextState nsPolicy = new NextState();
			
			int[] policyAction = policy.getAction(sample.nextState);
			nsPolicy.generateNextState(sample.nextState, policyAction);

			policyArray[i] = policy.getFeatureScores(nsPolicy);

			rewardsArray[i][0] = sample.reward;
		}

		Matrix policyMatrix = new Matrix(policyArray);
		Matrix rewardsMatrix = new Matrix(rewardsArray);
		
		Matrix LHSMatrix = samplesMatrix.transpose().times(samplesMatrix.minus(policyMatrix));
		Matrix RHSMatrix = samplesMatrix.transpose().times(rewardsMatrix);
		
		Matrix weightsMatrix = LHSMatrix.solve(RHSMatrix);

		int j = 0;
		for (int i = 0; i < policy.heuristic.features.length; i++) {
			if (i == 0 || i == 8 || i == 9 || i == 10) {
				continue;
			}
			policy.heuristic.features[i].setFeatureWeight(weightsMatrix.get(j, 0));
			j++;
		}
	}
	
	public void generateSamples(int count) {
		samples = new Sample[count];
		Random random = new Random();

		for (int i = 0; i < count; i++) {
			NextState state = NextState.generateRandomState();
			int[][] moves = state.legalMoves();
			samples[i] = new Sample(state, moves[random.nextInt(moves.length)]);
		}
	}
	
	public static void main(String[] args) {
		LSPI lspi = new LSPI(1000000);
		
		for (int i = 0; i < 100; i++) {
			Feature[] features = lspi.policy.heuristic.features;
			
			for (int j = 0; j < features.length; j++) {
				if (j == 0 || j == 8 || j == 9 || j == 10) {
					continue;
				}
				System.out.println(features[j].getFeatureWeight());
			}

			double results = (new PlayerSkeleton()).playFullGame(lspi.policy.heuristic, false);
			System.out.println("Generation " + i + " cleared " + results + " rows.");
			System.out.println("---");

			lspi.nextIteration();
		}
	}
}
