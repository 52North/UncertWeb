function checkRange(min, max, value) {
    var message;
    if (value < min || value > max) {
        message = 'Value out of range [' + min + ', ' + max + ']: ' + value;
        throw new Error(message);
    }
    return value;
}

function degToRad(x) { return x * (180/Math.PI); }
function radToDeg(x) { return x * (Math.PI/180); }

function Color() {}

Color.prototype.toHex = function() {
    return '#' + this.toRGB().toArray().map(function(x) {
    		var s = x.toString(16).toUpperCase();
    		return s.length < 2 ? '0' + s : s;
        }).join('');
};

Color.prototype.toHSB = function() {
    return this.toHSV();
};

Color.prototype.toHLS = function() {
    return this.toHSL();
};

function RGB(red, green, blue) {
    Color.call(this);
    this.red = checkRange(0, 255, red);
    this.green = checkRange(0, 255, green);
    this.blue = checkRange(0, 255, blue);
}

RGB.prototype = Object.create(Color.prototype);

RGB.prototype.toRGB = function() {
    return this;
};

RGB.prototype.toString = function() {
    return 'RGB(r=' + this.red + ',g=' + this.green + ',b=' + this.blue + ')';
};

RGB.prototype.toArray = function() {
    return [this.red, this.green, this.blue];
};

RGB.prototype.toHSI = function() {
    var r = this.red;
    var g = this.green;
    var b = this.blue;
    var i = (r + g + b)/3;
    var m = Math.min(r, g, b);
    var s = (i === 0) ? 0 : 1 - (m/i);
    var degree = (r - g/2 - b/2)/Math.sqrt(r*r + g*g + b*b - r*g - r*b - g*b);
    var h = radToDeg(Math.acos(degToRad(degree)));
    if (b > g) {
        h = 360 - h;
    }
    return new HSI(h, s, i);
};

RGB.prototype.toHSL = function() {
    var r = this.red/255;
    var g = this.green/255;
    var b = this.blue/255;
    var max = Math.max(r, g, b);
    var min = Math.min(r, g, b);
    var d = max - min;

    var h;
    var s;
    var l = (max + min) / 2;

    if (d === 0) {
        h = s = 0;
    } else {
        s = l > 0.5 ? d / (2 - max - min) : d / (max + min);
        switch (max) {
            case r:
                h = (g - b) / d + (g < b ? 6 : 0);
                break;
            case g:
                h = (b - r) / d + 2;
                break;
            case b:
                h = (r - g) / d + 4;
                break;
        }
        h /= 6;
    }
    return new HSL(h, s, l);
};

RGB.prototype.toHSV = function() {
    var r = this.red/255;
    var g = this.green/255;
    var b = this.blue/255;
    var max = Math.max(r, g, b);
    var min = Math.min(r, g, b);
    var d = max - min;

    var h;
    var s = (max === 0) ? 0 : d/max;
    var v = max;

    if (d === 0) {
        h = 0;
    } else {
        switch (max) {
            case r:
                h = 60 * (((g - b) / d) % 6);
                break;
            case g:
                h = 60 * (((b - r) / d) + 2);
                break;
            case b:
                h = 60 * (((r - g) / d) + 4);
                break;
        }
    }
    return new HSV(h, s, v);
};

RGB.fromHex = function fromHex(hex) {
    if (hex.charAt(0) === '#') {
        hex = hex.substring(1);
    }
    var r = parseInt(hex.substring(0, 2), 16);
    var g = parseInt(hex.substring(2, 4), 16);
    var b = parseInt(hex.substring(4, 6), 16);
    return new RGB(r, g, b);
};


function HSV(hue, saturation, value) {
    Color.call(this);
    this.hue = checkRange(0, 360, hue);
    this.saturation = checkRange(0, 1, saturation);
    this.value = checkRange(0, 1, value);
}

HSV.prototype = Object.create(Color.prototype);

HSV.prototype.toHSV = function() {
    return this;
};

HSV.prototype.toHSI = function() {
    return this.toRGB().toHSI();
};

HSV.prototype.toHSL = function() {
    return this.toRGB().toHSL();
};

HSV.prototype.toString = function() {
    return 'HSV(h=' + this.hue + ',s=' + this.saturation + ',v=' + this.value + ')';
};

HSV.prototype.toArray = function() {
    return [this.hue, this.saturation, this.value];
};

HSV.prototype.toRGB = function() {
    var h = this.hue/360;
    var s = this.saturation;
    var v = this.value;
    var r = null, g = null, b = null;
    var i, x, y, z;
    if (s === 0) {
        r = g = b = v;
    } else {
        i = Math.floor(h * 6);
        x = v * (1 - s);
        y = v * (1 - s * ((h * 6) - i));
        z = v * (1 - s * (1 - ((h * 6) - i)));
        switch (i) {
            case 0: r = v; g = z; b = x; break;
            case 1: r = y; g = v; b = x; break;
            case 2: r = x; g = v; b = z; break;
            case 3: r = x; g = y; b = v; break;
            case 4: r = z; g = x; b = v; break;
            case 5: r = v; g = x; b = y; break;
        }
    }
    return new RGB(Math.round(r * 255),
                   Math.round(g * 255),
                   Math.round(b * 255));
};

function HSL(hue, saturation, lightness) {
    Color.call(this);
    this.hue = checkRange(0, 360, hue);
    this.saturation = checkRange(0, 1, saturation);
    this.lightness = checkRange(0, 1, lightness);
}

HSL.prototype = Object.create(Color.prototype);

HSL.prototype.toHSV = function() {
    return this.toRGB().toHSV();
};

HSL.prototype.toHSI = function() {
    return this.toRGB().toHSI();
};

HSL.prototype.toHSL = function() {
    return this;
};

HSL.prototype.toString = function() {
    return 'HSL(h=' + this.hue + ',s=' + this.saturation + ',l=' + this.lightness + ')';
};

HSL.prototype.toArray = function() {
    return [this.hue, this.saturation, this.lightness];
};

function hueToRGBValue(p, q, t){
    if (t < 0) {
        t += 1;
    } else if (t > 1) {
        t -= 1;
    }
    if (t < 1/6) {
        return p + (q - p) * 6 * t;
    } else if (t < 1/2) {
        return q;
    } else  if (t < 2/3) {
        return p + (q - p) * (2/3 - t) * 6;
    } else {
        return p;
    }
}

HSL.prototype.toRGB = function() {
    var h = this.hue;
    var s = this.saturation;
    var l = this.lightness;
    var r, g, b;
    var p, q;

    if(s === 0) {
        r = g = b = l;
    } else {
        q = l < 0.5 ? l * (1 + s) : l + s - l * s;
        p = 2 * l - q;
        r = hueToRGBValue(p, q, h + 1/3);
        g = hueToRGBValue(p, q, h);
        b = hueToRGBValue(p, q, h - 1/3);
    }

    return new RGB(Math.round(r * 255),
                   Math.round(g * 255),
                   Math.round(b * 255));
};


function HSI(hue, saturation, intensity) {
    Color.call(this);
    this.hue = checkRange(0, 360, hue);
    this.saturation = checkRange(0, 1, saturation);
    this.intensity = checkRange(0, 255, intensity);
}

HSI.prototype = Object.create(Color.prototype);

HSI.prototype.toHSV = function() {
    return this.toRGB().toHSV();
};

HSI.prototype.toHSI = function() {
    return this;
};

HSI.prototype.toHSL = function() {
    return this.toRGB().toHSL();
};

HSI.prototype.toString = function() {
    return 'HSI(h=' + this.hue + ',s=' + this.saturation + ',i=' + this.intensity + ')';
};

HSI.prototype.toArray = function() {
    return [this.hue, this.saturation, this.intensity];
};

HSI.prototype.toRGB = function() {
    var h = this.hue;
    var s = this.saturation;
    var i = this.intensity;
    var cos = function(d) {
        return Math.cos(deg2rad(d));
    };
    var r = 0, g = 0, b = 0;
    if (h === 0) {
        r = i + 2 * i * s;
        g = b = i - i * s;
    } else if (h < 120) {
        r = i + i * s * cos(h) / cos(60 - h);
        g = i + i * s * (1 - cos(h) / cos(60 - h));
        b = i - i * s;
    } else if (h == 120) {
        r = b = i - i * s;
        g = i + 2 * i * s;
    } else if (h < 240) {
        r = i - i * s;
        g = i + i * s * cos(h - 120) / cos(180 - h);
        b = i + i * s * (1 - cos(h - 120) / cos(180 - h));
    } else if (h == 240) {
        r = g = i - i * s;
        b = i + s * i * s;
    } else {
        r = i + i * s * (1 - cos(h - 240) / cos(300 - h));
        g = i - i * s;
        b = i + i * s * cos(h - 240) / cos(300 - h);
    }
    return new RGB(Math.round(r * 255),
                   Math.round(g * 255),
                   Math.round(b * 255));
};

module.exports.hsi = function(h, s, i) { return new HSI(h, s, i); };
module.exports.hsl = function(h, s, l) { return new HSL(h, s, l); };
module.exports.hls = module.exports.hsl;
module.exports.hsv = function(h, s, v) { return new HSV(h, s, v); };
module.exports.hsb = module.exports.hsv;
module.exports.rgb = function(r, g, b) { return new RGB(r, g, b); };
module.exports.fromHex = RGB.fromHex;