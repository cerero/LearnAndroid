#ifdef LIGHTING_SHADER

   uniform mediump mat4 MODELVIEWMATRIX;

   uniform mediump mat4 PROJECTIONMATRIX;

   uniform mediump mat3 NORMALMATRIX;

   attribute lowp vec3 NORMAL;

   varying lowp vec3 normal;

   varying mediump vec3 position;
#else 

   uniform mediump mat4 MODELVIEWPROJECTIONMATRIX;
#endif

attribute mediump vec3 POSITION;

attribute mediump vec2 TEXCOORD0;

varying mediump vec2 texcoord0;

void main( void )
{
   texcoord0 = TEXCOORD0;

   #ifdef LIGHTING_SHADER
      position = vec3( MODELVIEWMATRIX * vec4( POSITION, 1.0 ) );

      normal = normalize( NORMALMATRIX * NORMAL );

      gl_Position = PROJECTIONMATRIX * vec4( position, 1.0 );
   #else 

      gl_Position = MODELVIEWPROJECTIONMATRIX * vec4( POSITION, 1.0 );
   #endif
}
