varying mediump vec2 texcoord0;


uniform sampler2D DIFFUSE;


varying lowp vec3 lightcolor;


void main( void ) {
	
	gl_FragColor = texture2D( DIFFUSE, texcoord0 ) * vec4( lightcolor, 1.0 ) + vec4( 0.1 );

}