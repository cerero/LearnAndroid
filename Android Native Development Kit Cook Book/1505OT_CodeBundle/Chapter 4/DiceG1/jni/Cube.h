#ifndef CUBE_H_
#define CUBE_H_

#include <GLES/gl.h>

class Cube {
private:
	GLfloat vertices[108];
	GLfloat texCoords[12];
	GLbyte indices[6];
	GLuint texIds[6];
public:
	Cube(GLfloat[], GLfloat[], GLbyte[]);
	void setTexCoords(GLuint[]);
	void draw();
	virtual ~Cube();
};

#endif /* CUBE_H_ */
