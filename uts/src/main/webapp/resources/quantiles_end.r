# add data to ncdf file
for(i in 1:length(quants)){
	# invert y coordinates as geotiff starts at ULC, netcdf at LLC
	put.var.ncdf(nc.sim, simdata,q@data[order(cc[,2]),i],
		start=c(1,1,i),count=c(length(xs),length(ys),1))
}
close.ncdf(nc.sim)