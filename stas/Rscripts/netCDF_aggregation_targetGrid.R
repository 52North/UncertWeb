update.packages("spacetime")
library(UncertWeb)

#read original file
file <- "D:/uncertwebWorkspace/stasTrunk/data/biotemperature_normalDistr.nc"
spUNetCDF <- readUNetCDF(file, variables=c("biotemperature_variance"))

#read file containing target grid
targetGridFile <- "D:/uncertwebWorkspace/stasTrunk/data/biotemperature_targetGrid.nc"
targetNetCDF <- readUNetCDF(targetGridFile, variables=c("biotemperature_mean")) #variables not needed; maybe adopt function!?

newGrid <- SpatialGrid(targetNetCDF@grid,targetNetCDF@proj4string)
newSpatialGrid <- as(newGrid,"SpatialGrid")
newPixels <- as(newSpatialGrid,"SpatialPixels")
summary(newPixels)

spAgg <- aggregate.Spatial(spUNetCDF,newPixels,mean)