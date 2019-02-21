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
	glVertexPointer(3, GL_FLOAT, 0, vertices);
	glDrawElements(GL_TRIANGLES, 36, GL_UNSIGNED_BYTE, indices);
	glDisableClientState(GL_VERTEX_ARRAY);
}

void Cube::lighting() {
	GLfloat cubeOneAmbientFraction[4] = {0.5, 0.5, 0.5, 1.0};
	GLfloat cubeOneDiffuseFraction[4] = {0.8, 0.0, 0.0, 1.0};
	GLfloat cubeSpecularFraction[4] = {0.0, 0.0, 0.0, 1.0};
	GLfloat cubeEmissionFraction[4] = {0.0, 0.0, 0.0, 1.0};
	glMaterialfv(GL_FRONT_AND_BACK, GL_AMBIENT, cubeOneAmbientFraction);
	glMaterialfv(GL_FRONT_AND_BACK, GL_DIFFUSE, cubeOneDiffuseFraction);
	glMaterialfv(GL_FRONT_AND_BACK, GL_SPECULAR, cubeSpecularFraction);
	glMaterialfv(GL_FRONT_AND_BACK, GL_EMISSION, cubeEmissionFraction);
	glMaterialf(GL_FRONT_AND_BACK, GL_SHININESS, 60.0);
}

Cube::~Cube() {

}


