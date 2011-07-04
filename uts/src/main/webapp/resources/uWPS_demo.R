# 1) script for UTS
##### Sampling from gaussian distribution #####
## parameters for sampling
# number of samples
i = 10000 
# gaussian distribution parameters
m.gauss = 20
sd.gauss = 5

# sampling
samples = rnorm(i, m.gauss, sd.gauss)


# 2) script for model WPS
##### Simple regression model for #####
## parameters for model
input = 20

# model
output = 2.1 + input*1.3

# 3) script for UTS
##### Building distribution from realisations #####
## parameters for estimation of distribution
# this is just an example how to create realisations, the true samples will be the WPS outputs
realisations = rnorm(10000, 25, 5)

# get realisation mean and standard deviation
m.realisations = mean(realisations)
s.realisations = sd(realisations)

# Maximum likelihood estimation for distribution parameters
norm.dist <-function(m=m.realisations,s=s.realisations){
	-sum(log(dnorm(realisations,m,s)))
}

# results for mean and standard deviation
m.gauss.est = mle(norm.dist)@coef[1]
s.gauss.est = mle(norm.dist)@coef[2]


