	.data
buffer:
	.asciiz "  "
	.text
main:
	jal L0
	li $v0, 10
	syscall
L0:
	move $t0, $ra
	move $t1, $fp
	addi $sp, $sp, -20
	sw $t0, 4($sp)
	sw $t1, 8($sp)
	addi $fp, $sp, 20
	li $t0, 3
	sub $sp, 4
	sw $t0, 4($sp)
	lw $t0, 4($sp)
	add $sp, 4
	sll $t0, $t0, 2
	li $v0, 9
	move $a0, $t0
	syscall
	move $t0, $v0
	sub $sp, 4
	sw $t0, 4($sp)
	lw $t0, 4($sp)
	add $sp, 4
	sw $t0, -12($fp)
	lw $t0, -12($fp)
	sub $sp, 4
	sw $t0, 4($sp)
	li $t0, 0
	sub $sp, 4
	sw $t0, 4($sp)
	li $t0, 10
	sub $sp, 4
	sw $t0, 4($sp)
	lw $t0, 4($sp)
	add $sp, 4
	lw $t1, 4($sp)
	add $sp, 4
	lw $t2, 4($sp)
	add $sp, 4
	sll $t1, $t1, 2
	addu $t2, $t2, $t1
	sw $t0, 0($t2)
	lw $t0, -12($fp)
	sub $sp, 4
	sw $t0, 4($sp)
	li $t0, 1
	sub $sp, 4
	sw $t0, 4($sp)
	li $t0, 20
	sub $sp, 4
	sw $t0, 4($sp)
	lw $t0, 4($sp)
	add $sp, 4
	lw $t1, 4($sp)
	add $sp, 4
	lw $t2, 4($sp)
	add $sp, 4
	sll $t1, $t1, 2
	addu $t2, $t2, $t1
	sw $t0, 0($t2)
	lw $t0, -12($fp)
	sub $sp, 4
	sw $t0, 4($sp)
	li $t0, 2
	sub $sp, 4
	sw $t0, 4($sp)
	lw $t0, -12($fp)
	sub $sp, 4
	sw $t0, 4($sp)
	li $t0, 0
	sub $sp, 4
	sw $t0, 4($sp)
	lw $t1, 4($sp)
	add $sp, 4
	lw $t0, 4($sp)
	add $sp, 4
	sll $t1, $t1, 2
	addu $t0, $t0, $t1
	lw $t0, 0($t0)
	sub $sp, 4
	sw $t0, 4($sp)
	lw $t0, -12($fp)
	sub $sp, 4
	sw $t0, 4($sp)
	li $t0, 1
	sub $sp, 4
	sw $t0, 4($sp)
	lw $t1, 4($sp)
	add $sp, 4
	lw $t0, 4($sp)
	add $sp, 4
	sll $t1, $t1, 2
	addu $t0, $t0, $t1
	lw $t0, 0($t0)
	sub $sp, 4
	sw $t0, 4($sp)
	lw $t1, 4($sp)
	add $sp, 4
	lw $t0, 4($sp)
	add $sp, 4
	add $t0, $t0, $t1
	sub $sp, 4
	sw $t0, 4($sp)
	lw $t0, 4($sp)
	add $sp, 4
	lw $t1, 4($sp)
	add $sp, 4
	lw $t2, 4($sp)
	add $sp, 4
	sll $t1, $t1, 2
	addu $t2, $t2, $t1
	sw $t0, 0($t2)
	lw $t0, -12($fp)
	sub $sp, 4
	sw $t0, 4($sp)
	li $t0, 2
	sub $sp, 4
	sw $t0, 4($sp)
	lw $t1, 4($sp)
	add $sp, 4
	lw $t0, 4($sp)
	add $sp, 4
	sll $t1, $t1, 2
	addu $t0, $t0, $t1
	lw $t0, 0($t0)
	sub $sp, 4
	sw $t0, 4($sp)
	lw $a0, 4($sp)
	add $sp, 4
	jal entryPrintInt
	sw $v0, -16($fp)
	li $t0, 0
	sub $sp, 4
	sw $t0, 4($sp)
	lw $t0, 4($sp)
	add $sp, 4
	sw $t0, -20($fp)
	j L1
L1:
L1:
	lw $v0, -20($fp)
	addi $sp, $fp, -20
	lw $ra, 4($sp)
	lw $fp, 8($sp)
	jr $ra

entryPrintInt:
	li $v0, 1
	syscall
exitPrintInt:	
	j $ra

entryReadInt:
	li $v0, 5
	syscall
exitReadInt:
	j $ra

entryReadBool:
	la $a0, buffer
	li $a1, 3
	li $v0, 8
	syscall
	lb $v0, buffer
	li $t1, 84
	seq $v0, $v0, $t1
exitReadBool:
	j $ra
