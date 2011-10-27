
#Load libraries needed
library(spacetime)
library(rgeos)
library(maptools)
library(ncdf)
library(spatstat)

############################
##read NetCDF File
nc = open.ncdf("biotemperature_normalDistr.nc")
summary(nc)

#################
##extract variableNames
#varNames=NULL
#for(i in 1:length(nc$var)){
#  varNames<-c(varNames,nc$var[[i]]$name)
#}

#get X and Y coordinates
lon = nc$dim[[1]]$vals
lat = nc$dim[[2]]$vals
nx = length(lon)
ny = length(lat)
n=length(lon)*length(lat)

#fill data
points.coords = data.frame(lon=1:n, lat=1:n)
data.bio.mean = 1:n
for(x in 1:nx){
  points.coords$lon[((x-1)*ny+1):(ny*x)] = rep(lon[x],ny)
	points.coords$lat[((x-1)*ny+1):(ny*x)] = lat
	start = c(x,1)
	data.bio.mean[((x-1)*ny+1):(ny*x)]  = get.var.ncdf(nc, "biotemperature_mean", start=start, count=c(1,ny)) 
}

#create dataframes
points.mean = SpatialPixelsDataFrame(points=points.coords, data=as.data.frame(data.bio.mean))
spdf = as(points.mean, "SpatialGridDataFrame")

#convert to a "im" object
map.im <- as.im.SpatialGridDataFrame(spdf)
#calculate weighted means
blur.map <- blur(map.im, 2 * map.im$xstep, bleed=F, normalize=T)
#change the dimension of the pixel image
map.new <- as.im(blur.map,dimyx=(floor(map.im$dim)/2))
#change them back into SpatialGridDataFrame 
original <- as.SpatialGridDataFrame.im(map.im)
blured <- as.SpatialGridDataFrame.im(blur.map)
resampled <- as.SpatialGridDataFrame.im(map.new)
spplot(original)
spplot(resampled)
spplot(blured)
class(resampled)
summary(resampled)
summary(original)
par(mfrow=c(3,1))
?blur
#make one plot of all three stages for comparison
image(original)
image(blured)
image(resampled)



###########
#convert O&M in csv output to SpatioTemporalDataFrame
#set working directory
setwd("D:/geostatws")

#read csv file into data frame
aqValues <- read.csv("csvOutput.txt")
summary(aqValues)

stConstruct(aqValues,c(2,3),1)


#extract dates from data frame
dts <- as.Date(aqValues$PhenomenonTime)
summary(dts)
class(dts)
length(dts)

#extract column with WKT geometries
geoms <- aqValues$WKTGeometry
summary(geoms)

geomsUnique <- unique(geoms)
length(geomsUnique)
#read first geometry; then iterate over all geometries and put them into one SpatialX object
spatialGeoms <- readWKT(geoms[1])
for(i in 2:geoms.length) {
  geom <- readWKT(geoms[i])
  spatialGeoms<-spRbind(spatialGeoms,geom)
}
spatialGeoms
summary(spatialGeoms)
aq_stidf <- STIDF(spatialGeoms,dts,aqValues)


summary(aq_stidf)
plot(aq_stidf)

rename.vars(aqValues, from="fid", to="Id", info=TRUE)
measurements = STIDF(lines,dts,aqValues)
summary(aqValuesLines)
