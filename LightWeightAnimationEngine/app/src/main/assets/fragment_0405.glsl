#ifdef LIGHTING_SHADER

	uniform mediump vec3 LIGHTPOSITION;

	uniform lowp vec3 AMBIENT_COLOR;

	uniform lowp vec3 DIFFUSE_COLOR;

	uniform lowp vec3 SPECULAR_COLOR;

	uniform mediump float SHININESS;

	uniform lowp float DISSOLVE;

	varying mediump vec3 position;

	varying lowp vec3 normal;
#endif

uniform sampler2D DIFFUSE;

varying mediump vec2 texcoord0;

void main( void )
{
	lowp vec4 diffuse_color = texture2D( DIFFUSE, texcoord0 );

	#ifdef LIGHTING_SHADER

		lowp float alpha = diffuse_color.a;

		mediump vec3 L = normalize( LIGHTPOSITION - position );

		mediump vec3 E = normalize( -position );

		mediump vec3 R = normalize( -reflect( L, normal ) );

		mediump vec4 ambient  = vec4( AMBIENT_COLOR, 1.0 );

		mediump vec4 diffuse  = vec4( DIFFUSE_COLOR * 
									  diffuse_color.rgb, 1.0 ) * 
									  max( dot( normal, L ), 0.0 );

		mediump vec4 specular = vec4( SPECULAR_COLOR, 1.0 ) * 
									  pow( max( dot( R, E ), 0.0 ), 
									  SHININESS * 0.3 );

		diffuse_color = vec4( 0.1 ) +
						ambient +
						diffuse +
						specular;

		diffuse_color.a = alpha;

	#endif	

	#ifdef SOLID_OBJECT
	
		gl_FragColor = diffuse_color;
	#endif

	#ifdef ALPHA_TESTED_OBJECT

		if( diffuse_color.a < 0.1 ) discard;
	  
		else gl_FragColor = diffuse_color;
	#endif

	#ifdef TRANSPARENT_OBJECT
		
		gl_FragColor = diffuse_color;		
		gl_FragColor.a = DISSOLVE;
	#endif
}