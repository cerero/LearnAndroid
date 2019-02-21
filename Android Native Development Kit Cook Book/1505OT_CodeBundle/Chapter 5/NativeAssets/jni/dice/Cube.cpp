#include "Cube.h"

Cube::Cube(GLfloat pVertices[], GLfloat pTexCoords[], GLbyte pIndices[]) {
	int i;
	for (i = 0; i < 108; ++i) {
		vertices[i] = pVertices[i];
	}
	for (i = 0; i < 12; ++i) {
		texCoords[i] = pTexCoords[i];
	}
	for (i = 0; i < 6; ++i) {
		indices[i] = pIndices[i];
	}

}

//once we rotate, the surface normal is going to change
void Cube::draw() {
	glEnableClientState(GL_VERTEX_ARRAY);
	glEnableClientState(GL_TEXTURE_COORD_ARRAY);  // Enable texture-coords-array
	glFrontFace(GL_CW);

	glBindTexture(GL_TEXTURE_2D, texIds[0]);
	glTexCoordPointer(2, GL_FLOAT, 0, texCoords);
	glVertexPointer(3, GL_FLOAT, 0, vertices);
	glDrawElements(GL_TRIANGLES, 18, GL_UNSIGNED_BYTE, indices);

	glBindTexture(GL_TEXTURE_2D, texIds[1]);
	glTexCoordPointer(2, GL_FLOAT, 0, texCoords);
	glVertexPointer(3, GL_FLOAT, 0, &vertices[18]);
	glDrawElements(GL_TRIANGLES, 18, GL_UNSIGNED_BYTE, indices);

	glBindTexture(GL_TEXTURE_2D, texIds[2]);
	glTexCoordPointer(2, GL_FLOAT, 0, texCoords);
	glVertexPointer(3, GL_FLOAT, 0, &vertices[36]);
	glDrawElements(GL_TRIANGLES, 18, GL_UNSIGNED_BYTE, indices);

	glBindTexture(GL_TEXTURE_2D, texIds[3]);
	glTexCoordPointer(2, GL_FLOAT, 0, texCoords);
	glVertexPointer(3, GL_FLOAT, 0, &vertices[54]);
	glDrawElements(GL_TRIANGLES, 18, GL_UNSIGNED_BYTE, indices);

	glBindTexture(GL_TEXTURE_2D, texIds[4]);
	glTexCoordPointer(2, GL_FLOAT, 0, texCoords);
	glVertexPointer(3, GL_FLOAT, 0, &vertices[72]);
	glDrawElements(GL_TRIANGLES, 18, GL_UNSIGNED_BYTE, indices);

	glBindTexture(GL_TEXTURE_2D, texIds[5]);
	glTexCoordPointer(2, GL_FLOAT, 0, texCoords);
	glVertexPointer(3, GL_FLOAT, 0, &vertices[90]);
	glDrawElements(GL_TRIANGLES, 18, GL_UNSIGNED_BYTE, indices);

	glDisableClientState(GL_VERTEX_ARRAY);
	glDisableClientState(GL_TEXTURE_COORD_ARRAY);
}

void Cube::setTexCoords(GLuint pTexIds[]) {
	int i;
	for (i = 0; i < 6; ++i) {
		texIds[i] = pTexIds[i];
	}
}

Cube::~Cube() {

}


