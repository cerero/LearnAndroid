	.arch armv5te
	.fpu softvfp
	.eabi_attribute 20, 1
	.eabi_attribute 21, 1
	.eabi_attribute 23, 3
	.eabi_attribute 24, 1
	.eabi_attribute 25, 1
	.eabi_attribute 26, 2
	.eabi_attribute 30, 6
	.eabi_attribute 18, 4
	.file	"tmp.c"
	.text
	.align	2
	.global	Java_cookbook_chapter2_AssemblyInJNIActivity_AssemblyMultiplyDemo
	.type	Java_cookbook_chapter2_AssemblyInJNIActivity_AssemblyMultiplyDemo, %function
Java_cookbook_chapter2_AssemblyInJNIActivity_AssemblyMultiplyDemo:
	@ args = 0, pretend = 0, frame = 24
	@ frame_needed = 1, uses_anonymous_args = 0
	@ link register save eliminated.
	str	fp, [sp, #-4]!
	add	fp, sp, #0
	sub	sp, sp, #28
	str	r0, [fp, #-16]
	str	r1, [fp, #-20]
	str	r2, [fp, #-24]
	str	r3, [fp, #-28]
	ldr	r2, [fp, #-24]
	ldr	r1, [fp, #-28]
	mul	r3, r1, r2
	str	r3, [fp, #-8]
	ldr	r3, [fp, #-8]
	mov	r0, r3
	add	sp, fp, #0
	ldmfd	sp!, {fp}
	bx	lr
	.size	Java_cookbook_chapter2_AssemblyInJNIActivity_AssemblyMultiplyDemo, .-Java_cookbook_chapter2_AssemblyInJNIActivity_AssemblyMultiplyDemo
	.ident	"GCC: (GNU) 4.4.3"
	.section	.note.GNU-stack,"",%progbits
