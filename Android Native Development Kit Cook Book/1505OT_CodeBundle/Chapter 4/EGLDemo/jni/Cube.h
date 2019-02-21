#ifndef CUBE_H_
#define CUBE_H_

#include <GLES/gl.h>

class Cube {
private:
	GLfloat vertices[24];
	GLfloat colors[32];
	GLbyte indices[36];
public:
	Cube(GLfloat[], GLfloat[], GLbyte[]);
	void draw();
	virtual ~Cube();
};

#endif /* CUBE_H_ */
