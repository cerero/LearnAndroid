#include "Square.h"

Square::Square(GLfloat pVertices[], GLfloat pColors[], GLbyte pIndices[]) {
	int i;
	for (i = 0; i < 12; ++i) {
		vertices[i] = pVertices[i];
	}
	for (i = 0; i < 16; ++i) {
		colors[i] = pColors[i];
	}
	for (i = 0; i < 6; ++i) {
		indices[i] = pIndices[i];
	}
}

void Square::draw() {
	// Enable vertex-array and define its buffer
	glEnableClientState(GL_VERTEX_ARRAY);
	glEnableClientState(GL_COLOR_ARRAY);
	glVertexPointer(3, GL_FLOAT, 0, vertices);
	glColorPointer(4, GL_FLOAT, 0, colors);
	// Draw the primitives from index array
	glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_BYTE, indices);
//	glDrawArrays(GL_TRIANGLE_STRIP, 0, 12 / 3);
	glDisableClientState(GL_VERTEX_ARRAY);
	glDisableClientState(GL_COLOR_ARRAY);
}

Square::~Square() {
}

