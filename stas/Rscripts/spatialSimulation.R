## reading UNetCDF
library(UncertWeb)
library(gstat)

# change to your needs, preferably to a local copy.
file <- "D:/uncertwebWorkspace/stasTrunk/data/biotemperature_normalDistr.nc"

spUNetCDF <- readUNetCDF(file, variables=c("biotemperature_mean"))
colnames(spUNetCDF@data) <- "biotemp" # 

# transform to points, have to check for kriging & blocks
spUNetCDF <- as(spUNetCDF,"SpatialPointsDataFrame")

# calculate empirical variogram
empVgm <- variogram(biotemp~1,spUNetCDF[!is.na(spUNetCDF$biotemp),],cutoff=800)

# fit an exponential model
fitVgm <- fit.variogram(empVgm,vgm(5,"Exp",800,0))

# plot empoirical and fitted variogram for visual inspection
plot(empVgm,fitVgm)

# unconditionally simulate data -> takes too much time for all!!
extent <- SpatialPolygons(list(Polygons(list(Polygon(matrix(c(5, 45,5,55,15,55,15,45,5,45),ncol=2,byrow=T))),ID="a")),proj4string=CRS(proj4string(spUNetCDF)))

subSet <- overlay(extent,spUNetCDF[!is.na(spUNetCDF$biotemp),])
subSet <- spUNetCDF[!is.na(spUNetCDF$biotemp),][!is.na(subSet),]

# only 400 randomly selected grid cells are simulated, due to computing time
simData <- krige(formula=biotemp~1, locations=NULL, 
                 newdata=as(subSet,"SpatialPoints")[sample(3343,size=400)], 
                 model=fitVgm, dummy=T, nsim=10, beta=mean(spUNetCDF$biotemp,na.rm=T))

# plot simulations 
gridded(simData) <- TRUE
spplot(simData, col.regions=rev(heat.colors(100)))

# won't work for ioncomplete grids by now, has to be chnaged!
writeUNetCDF(newfile="G:/data/spatial simulation.nc", simData)
str(writeUNetCDF)