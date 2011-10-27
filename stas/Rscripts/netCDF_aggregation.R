## reading UNetCDF
install.packages("RNetCDF")
library(RNetCDF)
library(spacetime)

# change to your needs, preferably to a local copy. However, writing to NetCDF is by defualt disabled.
file <- "D:/Tomcat6/apache-tomcat-6.0.32/temp/ncInput1319114818439.nc" 

#"//ifgifiles/projekte/UncertWeb/WP 3 Tools/Spatio-temporal Aggregation/Data/biotemperature_normalDistr.nc"

uncdf <- open.nc(file, write=F)
print.nc(uncdf) # gives:


spUNetCDF <- readUNetCDF(file, variables=c("biotemperature_variance"))

str(spUNetCDF)

spplot(spUNetCDF,col.regions=rev(heat.colors(100)))

# define new grid

scale <- 2 # factor of rescaling
newCellsize <- scale*spUNetCDF@grid@cellsize # rescaling the cell size
newCellcentre.offset <- spUNetCDF@bbox[,1]+newCellsize/2 # min of bbox + 1/2 new cellsize -> lower-left cellcentre
newDim <- ceiling(c(diff(spUNetCDF@bbox[1,])/newCellsize[1], diff(spUNetCDF@bbox[2,])/newCellsize[2])) # calculating the new dimensions. The new grid will most likely extend the old grid on the top-right corner

gridTopo <- GridTopology(cellcentre.offset=newCellcentre.offset, cellsize=newCellsize, cells.dim=newDim)
newGrid <- SpatialGrid(gridTopo, proj4string="+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs")
str(newGrid)

newGrid@bbox
spUNetCDF@bbox # is contained in the new bbox

newPixels <- as(newGrid,"SpatialPixels") # there seems to be a problem with grids, works fine for SpatialPixels
str(newPixels)

spAgg <- aggregate.Spatial(spUNetCDF,newPixels,mean)

str(spAgg)
spplot(spAgg)

writeUetCDF(newfile="~/newUNetCDF.nc", spAgg)
