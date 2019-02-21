#include "Triangle.h"

Triangle::Triangle(GLfloat pVertices[]) {
	int i;
	for (i = 0; i < 9; ++i) {
		vertices[i] = pVertices[i];
	}
}

void Triangle::draw() {
	// Enable vertex-array and define its buffer
	glEnableClientState(GL_VERTEX_ARRAY);
	glVertexPointer(3, GL_FLOAT, 0, vertices);
	glColor4f(0.5f, 0.5f, 0.5f, 0.5f);      //set the current color
	// Draw the primitives from the vertex array directly
	//void glDrawArrays(GLenum mode, GLint first, GLsizei count);
	glDrawArrays(GL_TRIANGLES, 0, 9/3);
	glDisableClientState(GL_VERTEX_ARRAY);
}

Triangle::~Triangle() {

}

