######################################################
######### Load input data from NetCDF file ##########
######################################################
par.sd = paste("u",par.mean,sep="")
# file has 6 variables
# variables: biotemperature, epratio, prec, ubiotemperature, uepratio, uprec

# get unit
for(i in 1:length(nc$var)){
	if(nc$var[[i]]$name==par.mean)
		unit = nc$var[[i]]$units
}

# get X and Y coordinates
lon = nc$dim[[1]]$vals
lat = nc$dim[[2]]$vals
nx = length(lon)
ny = length(lat)
n=length(lon)*length(lat)

# necessary variables for SpatialPixelsDataFrames
points.coords = data.frame(lon=1:n, lat=1:n)
data.mean = 1:n
data.sd = 1:n

# get data from netcdf file
for(x in 1:nx){
	points.coords$lon[((x-1)*ny+1):(ny*x)] = rep(lon[x],ny)
	points.coords$lat[((x-1)*ny+1):(ny*x)] = lat
	start = c(x,1)
	data.mean[((x-1)*ny+1):(ny*x)]  = get.var.ncdf(nc, par.mean, start=start, count=c(1,ny)) 
	data.sd[((x-1)*ny+1):(ny*x)]  = get.var.ncdf(nc, par.sd, start=start, count=c(1,ny)) 
}

# make SpatialPixelsDataFrames
points.mean = SpatialPixelsDataFrame(points=points.coords, data=as.data.frame(data.mean))
points.sd = SpatialPixelsDataFrame(points=points.coords, data=as.data.frame(data.sd))

# SpatialGridDataFrames
spdf = as(points.mean, "SpatialGridDataFrame")
uspdf = as(points.sd, "SpatialGridDataFrame")

# clean up workspace
close.ncdf(nc)
rm(points.mean,points.sd,start,x,nc,lon,lat,nx,ny,n,points.coords,data.mean,data.sd)