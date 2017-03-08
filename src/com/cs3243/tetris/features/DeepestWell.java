package com.cs3243.tetris.features;

import com.cs3243.tetris.NextState;

public class DeepestWell extends Feature {

	@Override
	public double getScore(NextState s) {
		int[] top = s.getTop();

		int maxWellDepth = 0;

		for (int i = 0; i < top.length; i++) {
			int wellDepth = 0;

			if (i == 0 || i == top.length - 1) {
				if(i == 0 && top[i + 1] > top[i]){
					wellDepth = top[i+1] - top[i];
				} else if(i == top.length -1 && top[i-1] > top[i]){
					wellDepth = top[i-1] - top[i];
				}
			} else if (top[i - 1] > top[i] && top[i + 1] > top[i]) {
				wellDepth = Math.min(top[i-1], top[i+1]) - top[i]; 
			}

			maxWellDepth = Math.max(maxWellDepth, wellDepth);
		}

		return featureWeight * maxWellDepth;
	}
}
