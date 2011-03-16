##############################################################
###### Script: Make quantiles for uncertain input data #####
##############################################################

##################################################################
############## calculate exceedance probabilities ###############
##################################################################

p = spdf
for(i in 1:length(probs)){
	p[[i]] <- 1-pnorm(probs[i], spdf[[1]], uspdf[[1]])
}

####################################################
########### write Output to NetCDF file ###########
####################################################
# sim, x and y dimensions
cc = coordinates(p)
xs = sort(unique(cc[,1]))
ys = sort(unique(cc[,2]))

# define dimension and variable
xdim <- dim.def.ncdf( "Lon", "degreesE", as.double(xs))
ydim <- dim.def.ncdf( "Lat", "degreesN", as.double(ys))

# specify quantile dimension
# dimension gets quantile values
simdim = dim.def.ncdf("thresholds", unit, probs)
simdata = var.def.ncdf(paste(par.mean,"_exceedanceProbability",sep=""), "probability", list(xdim,ydim, simdim), -999)