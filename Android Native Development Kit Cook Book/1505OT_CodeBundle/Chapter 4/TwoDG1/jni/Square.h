#ifndef SQUARE_H_
#define SQUARE_H_

#include <GLES/gl.h>

class Square {
private:
	GLfloat vertices[12];
	GLfloat colors[16];
	GLbyte indices[6];
public:
	Square(GLfloat[], GLfloat[], GLbyte[]);
	void draw();
	virtual ~Square();
};

#endif /* SQUARE_H_ */
