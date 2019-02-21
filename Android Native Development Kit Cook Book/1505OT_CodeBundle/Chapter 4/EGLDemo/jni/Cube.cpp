#include "Cube.h"

Cube::Cube(GLfloat pVertices[], GLfloat pColors[], GLbyte pIndices[]) {
	int i;
	for (i = 0; i < 24; ++i) {
		vertices[i] = pVertices[i];
	}
	for (i = 0; i < 32; ++i) {
		colors[i] = pColors[i];
	}
	for (i = 0; i < 36; ++i) {
		indices[i] = pIndices[i];
	}
}

//once we rotate, the surface normal is going to change
void Cube::draw() {
	// Enable vertex-array and define its buffer
	glEnableClientState(GL_VERTEX_ARRAY);
	glEnableClientState(GL_COLOR_ARRAY);
	glVertexPointer(3, GL_FLOAT, 0, vertices);
	glColorPointer(4, GL_FLOAT, 0, colors);
	glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_BYTE, indices);
	glDisableClientState(GL_VERTEX_ARRAY);
	glDisableClientState(GL_COLOR_ARRAY);
}

Cube::~Cube() {

}


