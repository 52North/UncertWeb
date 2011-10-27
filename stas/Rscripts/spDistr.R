# "d" -> pure density, no spatial correlation
# "r" -> simulate with gstat
# maybe from simulations, by now only mapped to the distribution:
# "p" -> probability per location to be below a certain threshold 
# "q" -> confidence bounds potentially derived from simulations

## stationary random variable
library(distr)
library(gstat)

setClass("spDistr",
  representation = representation(distr="Distribution", sp="Spatial", spDep="data.frame"),
)

spatialDistr <- function(distr, sp=NULL, spDep=vgm(0,"Nug",0,(distr@param@sd)^2)){
  if(is.null(sp)) {
    sp <- Spatial(matrix(0,2,2,dimnames=list(c("x","y"),c("min","max"))))
  }
  if ("variogramModel" %in% class(spDep)) spDep <- as.data.frame(spDep) # seems strange
  new("spDistr", distr=distr, sp=sp, spDep=spDep)  
}

simFunSpDistr <- function(object) {
  vgm <- object@spDep
  class(vgm) <- c("variogramModel","data.frame")
  beta <- object@distr@param@mean # has to be substituted with the expectation function provided by distr
  
  simFun <- function(n,points=NULL) {
              if(is.null(points)) points <- object@sp
              if("Spatial" %in% class(points)) {
                return(object@distr@r(n))
              } else {
                return(krige(formula=sim~1, locations=NULL, newdata=points,
                model=vgm, dummy=T, nsim=n, beta=beta))
              }
            }
  return(simFun)
}

setMethod(r,signature("spDistr"),simFunSpDistr)
setMethod(d,signature("spDistr"), function(object) return(object@distr@d))
setMethod(p,signature("spDistr"), function(object) return(object@distr@p))
setMethod(q,signature(save="spDistr"), function(save, status = 0, runLast = TRUE) return(save@distr@q))

# a spatial random variable without any points
spDistr <-  spatialDistr(Norm(12,4.3), spDep=vgm(psill=(4.3)^2-1,"Sph",range=400,1))

# loosely simulate without spatial reference
sims <- r(spDistr)(100)
hist(sims,n=20,freq=F)
curve(dnorm(x,12,4.3), add=T, col="red")

# getting some points
data(meuse)
coordinates(meuse) <- ~x+y

# simulate with spatial reference
spSims <- r(spDistr)(10,meuse)
spplot(spSims)
plot(variogram(sim1~1,spSims),vgm(psill=(4.3)^2-1,"Sph",range=400,1))

spSims <- r(spDistr)(100,meuse)
hist(as.numeric(spSims[1,]@data),n=20,freq=F)
curve(dnorm(x,12,4.3), add=T, col="red")

# points in distr
spDistr <-  spatialDistr(Norm(12,4.3), sp=as(meuse,"SpatialPoints"), spDep=vgm(psill=.8,"Sph",range=400,0.1))
emptySpatial <- Spatial(matrix(0,2,2,dimnames=list(c("x","y"),c("min","max"))))
r(spDistr)(10,points=emptySpatial)
spplot(r(spDistr)(10))