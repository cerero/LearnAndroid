#ifndef TRIANGLE_H_
#define TRIANGLE_H_

#include <GLES/gl.h>

class Triangle {
private:
	GLfloat vertices[9];
public:
	Triangle(GLfloat[]);
	void draw();
	virtual ~Triangle();
};

#endif /* TRIANGLE_H_ */
