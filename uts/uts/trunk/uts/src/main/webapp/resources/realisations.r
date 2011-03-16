
#################################################
############## make Realizations ###############
#################################################
# function for realizations
makeRealizations = function(spdf, uspdf, nsim = 10) {
# if variable name is null
  if (is.null(names(spdf))) names(spdf) = "var"
  sppdf = spdf
# make SpatialPointsDataFrame from SpatialGridDataFrame
  gridded(sppdf) = FALSE 
# number of cells in the raster
  nall = dim(sppdf)[1]

### sample 3000 cells from all cells ###
# random non-spatial sample
  spsdf = sppdf[sample(1:nall, 3000),]
# centering and scaling of pixel values
# centering: subtract overall average from all pixels
# scaling: dividing all pixels by overall standard deviation
  spsdf@data = as.data.frame(scale(spsdf@data)) 

### fit variogram to cell sample ###
# variogram for centered and scaled data from sample
  v.fit <- autofitVariogram(as.formula(paste(names(spsdf), "~1")), spsdf)
  vario = v.fit$var_model
  rang = vario$range[2]
# define number of new samples by range and grid area (minimum 5)
  nsamp = as.integer(areaSpatialGrid(spdf)/(rang*rang)) + 5
  # slocs = sppdf[sample(1:nall, nsamp),]

### new spatial sample from cell sample ###
# random spatial sample
  slocs = spsample(spsdf, nsamp, "random")
# get cell size in x and y direction from the original grid
  gp = gridparameters(spdf)
  dx = gp$cellsize[1]
  dy = gp$cellsize[2]
# shift coordinates by a random factor (-0.5 to 0.5) times the cell size
  slocs@coords = slocs@coords + 
     matrix( runif(nsamp*2) -0.5, ncol = 2)*matrix(c(rep(dx,nsamp),rep(dy,nsamp)),ncol=2)
# build spatial points dataframe from the sample with the new coordinates and values of 0
# this will serve as prediction locations
  slocs = SpatialPointsDataFrame(slocs, data = data.frame(dat = rep(0,nsamp)))

### Conditional simulation using the new sample locations ###
# creates a new grid based on the original grid with nsim simulations for residuals (mean=0)
  sims = krige(as.formula(paste(names(slocs), "~1")), slocs, spdf, vario, nsim = nsim, nmax = 8)
  sims2 = sims
  #sims2@data = spdf@data + sims@data * uspdf@data
# recalculate final results by multiplying scaled simulations with uncertainty grid and adding to mean grid
  sims2@data = as.data.frame(apply(sims@data, 2, rescale))
  return(sims2)
}

# helper function to recalculate values from residual simulations
rescale = function(df1){
	df = spdf@data+df1*uspdf@data
	return(df)
}

nsims = makeRealizations(spdf, uspdf, nsim = ns)

####################################################
########### write Output to NetCDF file ###########
####################################################
# sim, x and y dimensions
cc = coordinates(nsims)
xs = sort(unique(cc[,1]))
ys = sort(unique(cc[,2]))

# define dimension and variable
xdim <- dim.def.ncdf( "Lon", "degreesE", as.double(xs))
ydim <- dim.def.ncdf( "Lat", "degreesN", as.double(ys))

simdim = dim.def.ncdf("realisations", "id", 1:ns)
simdata = var.def.ncdf(par.mean, unit, list(xdim,ydim, simdim), -999)
