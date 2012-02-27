package org.uncertml.distribution.randomvariable;

import org.uncertml.statistic.CovarianceMatrix;

/**
 * wrapper for covariance matrix parameter
 * 
 * @author staschc
 *
 */
public class CovarianceMatrixParameter implements INormalCovarianceParameter {
		
		/** Covariance matrix of UncertML*/
		private CovarianceMatrix cvMatrix;
		
		/**
		 * constructor
		 * 
		 * @param cvMatrixp
		 * 		Covariance matrix of UncertML
		 */
		public CovarianceMatrixParameter(CovarianceMatrix cvMatrixp){
			this.cvMatrix = cvMatrixp;
		}

		/**
		 * @return the cvMatrix
		 */
		public CovarianceMatrix getCvMatrix() {
			return cvMatrix;
		}

		/**
		 * @param cvMatrix the cvMatrix to set
		 */
		public void setCvMatrix(CovarianceMatrix cvMatrix) {
			this.cvMatrix = cvMatrix;
		}
}
