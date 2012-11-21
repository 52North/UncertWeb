#############################################################################
################ Overlay of OM and NetCDF file #################
#############################################################################

library(RNetCDF)
library(spacetime)
library(rgdal)
library(rgeos)
library(maptools)


# function to read NetCDF-U files
readNetCDFU <- function(file, variables=NULL, time=NULL, realisation=NULL){
  uncdf <- open.nc(file, write=T)  
  
  ######## (1) get NetCDF-U file infos ########
  ### check for the right conventions ###
  conventionsStr <- NULL
  tryCatch(conventionsStr <- att.get.nc(uncdf, "NC_GLOBAL", "Conventions"))
  if(is.null(conventionsStr)){
    tryCatch(conventionsStr <- att.get.nc(uncdf, "NC_GLOBAL", "conventions"))
  }
  if(is.null(conventionsStr)) {
    warning("No conventions could be found for this NetCDF file!")
  }
  
  conventions <- strsplit(conventionsStr," ")[[1]]
  
  #"CF-1.5", "UW-1.0"
  climateForecast <- FALSE
  uncertWeb <- FALSE
  for(con in conventions){
    if(con=="CF-1.5"){
      climateForecast <- TRUE
    }
    if(con=="UW-1.0"){
      uncertWeb <- TRUE
    }
  }
  
  if(climateForecast==F) {
    warning("The NetCDF file does not fulfil the climate forecast 1.5 conventions!")
  }
  if(uncertWeb==F) {
    warning("The NetCDF file does not fulfil the UncertWeb 1.0 conventions!")
  }
  
  ### get primary variable(s) ###
  if(is.null(variables)){
    variables <- strsplit(att.get.nc(uncdf, "NC_GLOBAL", "primary_variables")," ")[[1]]
  } 
  
  ### check for projection ###
  test <- try(var.inq.nc(uncdf, "crs"))
  if (class(test) != "try-error"){
    # try to find the epsg code
    projection <- try(att.get.nc(uncdf, "crs", "epsg_code"))
    
    # else try to find the proj4string definition
    if(class(projection) == "try-error"|is.null(projection)){
      projection <- try(att.get.nc(uncdf, "crs", "proj4string"))
    }else{
      # for epsg code add string
      projection <- paste("+init=epsg:",projection,sep="")
    }
    
    # if both is not available throw an error
    if(class(projection) == "try-error"|is.null(projection)){
      x="lon"
      y="lat"
      warning("crs variable has not been correctly defined: epsg_code and proj4string are missing")
    }else{
      # check for x and y coordinates
      x <- try(var.get.nc(uncdf, "x"))
      y <- try(var.get.nc(uncdf, "y"))
      if (class(x) == "try-error"|is.null(x)| class(y) == "try-error"|is.null(y)){
        x="lon"
        y="lat"
        warning("projected x and y variables have not been correctly defined")       
      }else{
        x = "x"
        y = "y"
      }
    }
  }
    
  ### check for time ###
  if(is.null(time)){
    test <- try(var.inq.nc(uncdf, "time"))
    if (class(test) != "try-error"){
      time <- "time"
    }else{
      time <- NULL
    }
  }
  
  ### check for realisations ###
  if(is.null(realisation)){
    test <- try(var.inq.nc(uncdf, "realisation"))
    if (class(test) != "try-error"){
      realisation <- "realisation"   
    }else{
      test <- try(var.inq.nc(uncdf, "realization"))
      if (class(test) != "try-error"){
        realisation <- "realization"
      }else{
        realisation <- NULL
      }
    }
  }
  
  
  ######## (2) get NetCDF variables and dimensions ########
  ### dimensions ###
  xdim <- var.get.nc(uncdf, x)
  ydim <- var.get.nc(uncdf, y)
  if(!is.null(time)) {
    # get the unit and make a POSIXct object from the variable
    tunit <- att.get.nc(uncdf, time, "units")
    tunit.list <- strsplit(tunit, " ")[[1]]
    startDate = paste(tunit.list[3],tunit.list[4],tunit.list[5])
    if(tunit.list[1]=="days"){
      tdim <- as.POSIXct(startDate,tz="GMT")+24*3600*var.get.nc(uncdf,time)
    }else if(tunit.list[1]=="hours"){
      tdim <- as.POSIXct(startDate,tz="GMT")+3600*var.get.nc(uncdf,time)
    }else if(tunit.list[1]=="minutes"){
      tdim <- as.POSIXct(startDate,tz="GMT")+60*var.get.nc(uncdf,time)
    }else if(tunit.list[1]=="seconds"){
      tdim <- as.POSIXct(startDate,tz="GMT")+var.get.nc(uncdf,time)
    }        
  }else{
    tdim <- NULL
  }
  
  if(!is.null(realisation)){
    rdim <- var.get.nc(uncdf,realisation)
  }else{
    rdim <- NULL
  }

  ### get data from primary variables ###
  df <- NULL
  for(v in variables){
    # check if this variable contains coordinates, time and realisation
    dimsFound <- c(FALSE,FALSE,FALSE,FALSE)
    dims <- var.inq.nc(uncdf, v)$dimids
    for(d in dims){
      if(d==dim.inq.nc(uncdf,x)$id){
        dimsFound[1] = TRUE;
      }
      if(d==dim.inq.nc(uncdf,y)$id){
        dimsFound[2] = TRUE;
      } 
      if(d==dim.inq.nc(uncdf,realisation)$id){
        dimsFound[3] = TRUE;
      }
      if(d==dim.inq.nc(uncdf,time)$id){
        dimsFound[4] = TRUE;
      }
    }
    
    # only continue for valid variables
    if(sum(dimsFound)==4){
      df[[v]] <- as.numeric(var.get.nc(uncdf,v))
    }   
  }
  
  
  ######## (3) create dataframe  ########
  ### sort and create dataframe for making sp or st objects ###
  # simplest case
  if(is.null(time)&&is.null(realisation)){
    df <- as.data.frame(df)
  }else if(is.null(time)){
    # TODO: implement
  }else if(is.null(realisation)){
    # TODO: implement
  }else{
   for(j in 1:length(df)){  
        tmp <- NULL
        vdim <- 1:(length(df[[j]])/length(rdim))
        for(r in rdim){
          tmp <- cbind(tmp, df[[1]][(r-1)*length(vdim)+vdim])
        }        
    }
    var.name <- names(df)[1]
    df <- as.data.frame(tmp)
    names(df) <- paste(var.name, "_r",rdim, sep="")  
  }  
  
  ### create spatial object ###
  # if no time is available make simple SpatialDataFrame
  if(is.null(tdim)) {
    df$x <- rep(xdim,length(ydim))
    df$y <- rep(ydim,each=length(xdim))
    # how are projections commonly defined? I am just guessing here
    # "gridded" can so far as well only be guessed
    coordinates(df) <- ~x+y
    if(!is.null(projection)){
      proj4string(df) <- CRS(projection)
    }else{
      proj4string(df) <- CRS(projargs="+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs")
    }
    gridded(df) <- TRUE
  } else { # for time make SpatioTemporalFullDataFrame
    if(!is.null(projection)){
      sp <- SpatialPoints(coords=cbind(rep(xdim,length(ydim)),rep(ydim,each=length(xdim))), proj4string=CRS(projection))
    }else{
      sp <-SpatialPoints(coords=cbind(rep(xdim,length(ydim)),rep(ydim,each=length(xdim))), proj4string=CRS("+proj=longlat +ellps=WGS84 +datum=WGS84 +no_defs"))
    }
    gridded(sp) <- TRUE
    stfdf <- STFDF(sp=sp, time=tdim, data=df)
  }
 
  close.nc(uncdf)
  return(stfdf)
}

# function to read O&M csv file
readOMcsv <- function(file){
  #read csv file into data frame
  om.table <- read.csv(file)
  
  # extract column with WKT geometriess
  om.geom <- readWKT(om.table$WKTGeometry[1])
  for(i in 2:nrow(om.table)) {
    om.tmp <- readWKT(om.table$WKTGeometry[i])
    if(class(om.tmp)=="SpatialPolygons") {
      tryCatch(om.tmp@polygons[[1]]@ID <- as.character(round(as.numeric(om.tmp@polygons[[1]]@ID) + runif(1,1,1000000))),
               finally=om.tmp@polygons[[1]]@ID <- as.character(round(as.numeric(om.tmp@polygons[[1]]@ID) + runif(1,1,1000000))))
    }   
    om.geom<-spRbind(om.geom,om.tmp)
  }
  #row.names(om.geom) <- 1:nrow(coordinates(om.geom))
  if(om.table$EPSG[1]!=0){
    proj4string(om.geom) <- CRS(paste("+init=epsg:",om.table$EPSG[1],sep=""))
  }else{
    proj4string(om.geom) <- CRS("+init=epsg:4326")
  }  
  
  # get correct time
  tmp1 <- strsplit(as.character(om.table$PhenomenonTime),"/")
  if(length(tmp1[[1]])==2){
    tmp <- matrix(unlist(strsplit(as.character(om.table$PhenomenonTime),"/")),nrow=2)[1,]
  }else{
    tmp <- om.table$PhenomenonTime
  }
  time = matrix(unlist(strsplit(as.character(tmp),".000+")),ncol=2,byrow=T)
  om.time = as.POSIXct(strptime(time[,1], "%Y-%m-%dT%H:%M:%S"),tz="GMT")
  
  # add hours from time zone manually
  if(substring(time[1,2],1,1)=="+"){
    om.time = om.time - as.numeric(substring(time[,2],2,3))*3600
  }else if(substring(time[1,2],1,1)=="-"){
    om.time = om.time + as.numeric(substring(time[,2],2,3))*3600
  }
  
  # create om spacetime object
  om.st <- STI(om.geom[order(om.time)], om.time[order(om.time)])#- 2*365*24*3600)

  return(om.st)
}


# function to disaggregate from polygons to points
polygons2points <- function(polygons, pointDensity = 0.00001, minNumbPoints = 100){
  points <- NULL
  time.points <- NULL
  for(t in 1:length(polygons@time)){
    # create points by regurlar samples within each polygon,
    samples <- polygons@sp@polygons[t][[1]]@area*pointDensity
    
    # set a minimum number of points
    if(samples<minNumbPoints){samples=minNumbPoints}
    polygons.points <- spsample(polygons@sp[t], n=samples, type="regular")
    
    # add points for this timestep   
    if(t==1){
      points <- polygons.points
      time.points <- rep(index(polygons@time[t]),length(polygons.points))
    }else{
      points <- spRbind(points, polygons.points)
      time.points <- c(time.points,rep(index(polygons@time[t]),length(polygons.points)))
    }
    
    cat(paste("Finished time step",t,"\n"))
  }
  # make spatio-temporal dataframe from the points
  points.st <- STI(points, time.points)
  return(points.st)
}
