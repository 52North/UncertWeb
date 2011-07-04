#################################################
############## calculate quantiles ###############
#################################################

q = spdf
for(i in 1:length(quants)){
	q[[i]] <- qnorm(quants[i], spdf[[1]], uspdf[[1]])
}

####################################################
########### write Output to NetCDF file ###########
####################################################
# sim, x and y dimensions
cc = coordinates(q)
xs = sort(unique(cc[,1]))
ys = sort(unique(cc[,2]))

# define dimension and variable
xdim <- dim.def.ncdf( "Lon", "degreesE", as.double(xs))
ydim <- dim.def.ncdf( "Lat", "degreesN", as.double(ys))
# dimension gets quantile values
# specify quantile dimension
# dimension gets quantile values
simdim = dim.def.ncdf("quantiles", "level", quants)
simdata = var.def.ncdf(par.mean, unit, list(xdim,ydim, simdim), -999)
