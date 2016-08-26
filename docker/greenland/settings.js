/*
 * Copyright 2012 52°North Initiative for Geospatial Open Source Software GmbH
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
// OM conversion service url
if (typeof VIS == 'undefined')
    VIS = {};

VIS.omConversionServiceUrl = "http://localhost:8080/omcs";

// url of visualization service to use
VIS.vissUrl = "http://localhost:8080/viss";

VIS.wmsCapabilitiesProxy = "wmsproxy";
VIS.threddsProxy = "threddsproxy";

// Resources to show by default
VIS.defaultResources = [
{
    url : 'http://data/netCDF/targetNetCDF.nc',
    mime : 'application/netcdf'
},

];

VIS.nextResourceId = 0;
