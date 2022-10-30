/*
 * Copyright (c) 2022. Zen Liu.
 * SPDX-License-Identifier: GPL-2.0-only WITH Classpath-exception-2.0
 */

package cn.zenliu.assembler;

import lombok.AllArgsConstructor;
import lombok.Value;
import lombok.experimental.Delegate;
import net.bytebuddy.description.ByteCodeElement;
import net.bytebuddy.description.NamedElement;
import net.bytebuddy.description.annotation.AnnotationList;
import net.bytebuddy.description.field.FieldDescription;
import net.bytebuddy.description.field.FieldList;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.method.MethodList;
import net.bytebuddy.description.method.ParameterDescription;
import net.bytebuddy.description.method.ParameterList;
import net.bytebuddy.description.type.*;
import net.bytebuddy.implementation.Implementation;
import net.bytebuddy.implementation.bytecode.*;
import net.bytebuddy.implementation.bytecode.assign.Assigner;
import net.bytebuddy.implementation.bytecode.assign.InstanceCheck;
import net.bytebuddy.implementation.bytecode.assign.TypeCasting;
import net.bytebuddy.implementation.bytecode.assign.primitive.PrimitiveBoxingDelegate;
import net.bytebuddy.implementation.bytecode.assign.primitive.PrimitiveUnboxingDelegate;
import net.bytebuddy.implementation.bytecode.collection.ArrayAccess;
import net.bytebuddy.implementation.bytecode.collection.ArrayFactory;
import net.bytebuddy.implementation.bytecode.collection.ArrayLength;
import net.bytebuddy.implementation.bytecode.constant.*;
import net.bytebuddy.implementation.bytecode.member.FieldAccess;
import net.bytebuddy.implementation.bytecode.member.MethodInvocation;
import net.bytebuddy.implementation.bytecode.member.MethodReturn;
import net.bytebuddy.implementation.bytecode.member.MethodVariableAccess;
import net.bytebuddy.jar.asm.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import static cn.zenliu.assembler.Assembler.Manipulate.Simple.*;
import static net.bytebuddy.matcher.ElementMatchers.isAbstract;
import static net.bytebuddy.matcher.ElementMatchers.named;

/**
 * @author Zen.Liu
 * @since 2022-10-29
 */
@SuppressWarnings({"SpellCheckingInspection","unused"})
public interface Assembler {
    class Manipulates extends Stack<StackManipulation> {


    }
    class Stack<E> implements List<E>{
        @Delegate
        protected transient List<E> a;

        public Stack() {
            a=new ArrayList<>();
        }

        public void push(E v) {
            a.add(v);
        }
        public void removeElement(int from ,int to){
            a.removeAll(a.subList(from,to));
        }
        public E pop(){
            return a.remove(a.size()-1);
        }
    }
    class SimpleComposable implements Implementation.Composable {
        @Delegate
        final Implementation impl;

        public SimpleComposable(Implementation impl) {
            this.impl = impl;
        }

        public SimpleComposable(ByteCodeAppender bca) {
            this.impl = new Simple(bca);
        }

        @Override
        public Implementation andThen(Implementation implementation) {
            return new Compound(impl, implementation);
        }

        @Override
        public Composable andThen(Composable implementation) {
            return new Compound.Composable(impl, implementation);
        }
    }

    interface ByteCodeManipulates extends ByteCodeAppender {
        TypeDescription INT = TypeDescription.ForLoadedType.of(int.class);
        TypeDescription LONG = TypeDescription.ForLoadedType.of(long.class);
        TypeDescription FLOAT = TypeDescription.ForLoadedType.of(float.class);
        TypeDescription DOUBLE = TypeDescription.ForLoadedType.of(double.class);
        TypeDescription CHAR = TypeDescription.ForLoadedType.of(char.class);
        TypeDescription SHORT = TypeDescription.ForLoadedType.of(short.class);
        TypeDescription BYTE = TypeDescription.ForLoadedType.of(byte.class);
        TypeDescription BOOL = TypeDescription.ForLoadedType.of(boolean.class);


        abstract class HolderType extends TypeDescription.AbstractBase {

            @Override
            public Generic getSuperClass() {
                return null;
            }

            @Override
            public TypeList.Generic getInterfaces() {
                return null;
            }

            @Override
            public FieldList<FieldDescription.InDefinedShape> getDeclaredFields() {
                return null;
            }

            @Override
            public MethodList<MethodDescription.InDefinedShape> getDeclaredMethods() {
                return null;
            }

            @Override
            public RecordComponentList<RecordComponentDescription.InDefinedShape> getRecordComponents() {
                return null;
            }

            @Override
            public StackSize getStackSize() {
                return null;
            }

            @Override
            public boolean isArray() {
                return false;
            }

            @Override
            public boolean isRecord() {
                return false;
            }

            @Override
            public boolean isPrimitive() {
                return false;
            }


            @Override
            public TypeDescription getComponentType() {
                return null;
            }

            @Override
            public TypeDescription getDeclaringType() {
                return null;
            }

            @Override
            public TypeList getDeclaredTypes() {
                return null;
            }

            @Override
            public MethodDescription.InDefinedShape getEnclosingMethod() {
                return null;
            }

            @Override
            public TypeDescription getEnclosingType() {
                return null;
            }

            @Override
            public String getSimpleName() {
                return null;
            }

            @Override
            public String getCanonicalName() {
                return null;
            }

            @Override
            public boolean isAnonymousType() {
                return false;
            }

            @Override
            public boolean isLocalType() {
                return false;
            }

            @Override
            public PackageDescription getPackage() {
                return null;
            }

            @Override
            public TypeDescription getNestHost() {
                return null;
            }

            @Override
            public TypeList getNestMembers() {
                return null;
            }

            @Override
            public TypeList getPermittedSubtypes() {
                return null;
            }

            @Override
            public String getDescriptor() {
                return null;
            }

            @Override
            public String getName() {
                return null;
            }

            @Override
            public TypeList.Generic getTypeVariables() {
                return null;
            }

            @Override
            public AnnotationList getDeclaredAnnotations() {
                return null;
            }

            @Override
            public int getModifiers() {
                return 0;
            }
        }

        class LikeType extends HolderType {
            final TypeDescription[] targets;

            LikeType(TypeDescription... targets) {
                this.targets = targets;
            }

            @Override
            public boolean isAssignableFrom(TypeDescription typeDescription) {
                for (TypeDescription t : targets) {
                    if (t.isAssignableFrom(typeDescription)) return true;
                }
                return false;
            }

            @Override
            public boolean isAssignableTo(TypeDescription typeDescription) {
                for (TypeDescription t : targets) {
                    if (t.isAssignableTo(typeDescription)) return true;
                }
                return false;
            }

        }


        TypeDescription INT_LIKE = new LikeType(INT, FLOAT, LONG, DOUBLE, BOOL, SHORT, BYTE, CHAR);
        TypeDescription INT_NUM_LIKE = new LikeType(INT, FLOAT, LONG, DOUBLE, SHORT, BYTE, CHAR);
        TypeDescription TOP = new HolderType() {
        };

        @Override
        Size apply(MethodVisitor v, Implementation.Context c, MethodDescription m);

        default Implementation implementation() {
            return new Implementation.Simple(this);
        }

        default Implementation.Composable composable() {
            return new SimpleComposable(this);
        }

        default ByteCodeAppender compound(ByteCodeAppender... other) {
            if (other.length == 0) return new Compound(this);
            ByteCodeAppender[] o = new ByteCodeAppender[other.length + 1];
            o[0] = this;
            System.arraycopy(other, 0, o, 1, other.length);
            return new Compound(o);
        }


    }


    interface Manipulate extends StackManipulation {
        @Override
        default boolean isValid() {
            return true;
        }

        enum Simple implements Manipulate {
            IAND(Opcodes.IAND, StackSize.SINGLE),
            LAND(Opcodes.LAND, StackSize.DOUBLE),
            DCMPG(Opcodes.DCMPG, StackSize.DOUBLE),
            DCMPL(Opcodes.DCMPL, StackSize.DOUBLE),
            FCMPG(Opcodes.FCMPG, StackSize.SINGLE),
            FCMPL(Opcodes.FCMPL, StackSize.SINGLE),
            LCMP(Opcodes.LCMP, StackSize.DOUBLE),
            DNEG(Opcodes.DNEG, StackSize.DOUBLE),
            FNEG(Opcodes.FNEG, StackSize.SINGLE),
            INEG(Opcodes.INEG, StackSize.SINGLE),
            LNEG(Opcodes.LNEG, StackSize.DOUBLE),
            IOR(Opcodes.IOR, StackSize.SINGLE),
            LOR(Opcodes.LOR, StackSize.DOUBLE),
            SWAP(Opcodes.SWAP, StackSize.ZERO),
            ATHROW(Opcodes.ATHROW, StackSize.SINGLE),
            IXOR(Opcodes.IXOR, StackSize.SINGLE),
            LXOR(Opcodes.LXOR, StackSize.DOUBLE);
            private final int opcode;
            private final StackSize stackSize;

            Simple(int opcode, StackSize stackSize) {
                this.opcode = opcode;
                this.stackSize = stackSize;
            }

            @Override
            public Size apply(MethodVisitor methodVisitor, Implementation.Context implementationContext) {
                methodVisitor.visitInsn(opcode);
                return stackSize.toDecreasingSize();
            }


        }

        Size DEC1 = new Size(-1, 1);
        Size DEC2 = new Size(-2, 2);
        Size INC1 = new Size(1, 2);

        //@formatter:off
        Manipulate NOP=(v,c)->{v.visitInsn(Opcodes.NOP);return Size.ZERO;};
        //cast
//    Manipulate I2L=(v,c)->{v.visitInsn(Opcodes.I2L);return INC1;};
//    Manipulate I2F=(v,c)->{v.visitInsn(Opcodes.I2F);return Size.ZERO;};
//    Manipulate I2D=(v,c)->{v.visitInsn(Opcodes.I2D);return INC1;};
//    Manipulate L2I=(v,c)->{v.visitInsn(Opcodes.L2I);return DEC1;};
//    Manipulate L2F=(v,c)->{v.visitInsn(Opcodes.L2F);return DEC1;};
//    Manipulate L2D=(v,c)->{v.visitInsn(Opcodes.L2D);return Size.ZERO;};
//    Manipulate F2I=(v,c)->{v.visitInsn(Opcodes.F2I);return Size.ZERO;};
//    Manipulate F2L=(v,c)->{v.visitInsn(Opcodes.F2L);return INC1;};
//    Manipulate F2D=(v,c)->{v.visitInsn(Opcodes.F2D);return INC1;};
//    Manipulate D2I=(v,c)->{v.visitInsn(Opcodes.D2I);return DEC1;};
//    Manipulate D2L=(v,c)->{v.visitInsn(Opcodes.D2L);return Size.ZERO;};
//    Manipulate D2F=(v,c)->{v.visitInsn(Opcodes.D2F);return DEC1;};
//    Manipulate I2B=(v,c)->{v.visitInsn(Opcodes.I2B);return Size.ZERO;};
//    Manipulate I2C=(v,c)->{v.visitInsn(Opcodes.I2C);return Size.ZERO;};
//    Manipulate I2S=(v,c)->{v.visitInsn(Opcodes.I2S);return Size.ZERO;};

        //@formatter:on
        static Manipulate IINC(int varIdx, int v) {
            return (mv, c) -> {
                mv.visitIincInsn(varIdx, v);
                return Size.ZERO;
            };
        }

        static Manipulate GOTO(Label label) {
            return (v, c) -> {
                v.visitJumpInsn(Opcodes.GOTO, label);
                return Size.ZERO;
            };
        }

        static Manipulate IFNULL(Label label) {
            return (v, c) -> {
                v.visitJumpInsn(Opcodes.IFNULL, label);
                return DEC1;
            };
        }

        static Manipulate IFNONNULL(Label label) {
            return (v, c) -> {
                v.visitJumpInsn(Opcodes.IFNONNULL, label);
                return DEC1;
            };
        }

        //@formatter:off
        static Manipulate IF_ICMPEQ(Label label) {return (v, c) -> {v.visitJumpInsn(Opcodes.IF_ICMPEQ, label);return DEC2;};}
        static Manipulate IF_ICMPNE(Label label) {return (v, c) -> {v.visitJumpInsn(Opcodes.IF_ICMPNE, label);return DEC2;};}
        static Manipulate IF_ICMPLT(Label label) {return (v, c) -> {v.visitJumpInsn(Opcodes.IF_ICMPLT, label);return DEC2;};}
        static Manipulate IF_ICMPGE(Label label) {return (v, c) -> {v.visitJumpInsn(Opcodes.IF_ICMPGE, label);return DEC2;};}
        static Manipulate IF_ICMPGT(Label label) {return (v, c) -> {v.visitJumpInsn(Opcodes.IF_ICMPGT, label);return DEC2;};}
        static Manipulate IF_ICMPLE(Label label) {return (v, c) -> {v.visitJumpInsn(Opcodes.IF_ICMPLE, label);return DEC2;};}
        static Manipulate IF_ACMPEQ(Label label) {return (v, c) -> {v.visitJumpInsn(Opcodes.IF_ACMPEQ, label);return DEC2;};}
        static Manipulate IF_ACMPNE(Label label) {return (v, c) -> {v.visitJumpInsn(Opcodes.IF_ACMPNE, label);return DEC2;};}

        Function<Label,Manipulate> IFNONNULL=Manipulate::IFNONNULL;
        Function<Label,Manipulate> IFNULL=Manipulate::IFNULL;
        Function<Label,Manipulate> GOTO=Manipulate::GOTO;
        Function<Label,Manipulate> IF_ICMPEQ=Manipulate::IF_ICMPEQ;
        Function<Label,Manipulate> IF_ICMPNE=Manipulate::IF_ICMPNE;
        Function<Label,Manipulate> IF_ICMPLT=Manipulate::IF_ICMPLT;
        Function<Label,Manipulate> IF_ICMPGE=Manipulate::IF_ICMPGE;
        Function<Label,Manipulate> IF_ICMPGT=Manipulate::IF_ICMPGT;
        Function<Label,Manipulate> IF_ICMPLE=Manipulate::IF_ICMPLE;
        Function<Label,Manipulate> IF_ACMPEQ=Manipulate::IF_ACMPEQ;
        Function<Label,Manipulate> IF_ACMPNE=Manipulate::IF_ACMPNE;

        Function<Label,Manipulate> IFEQ=Manipulate::IFEQ;
        Function<Label,Manipulate> IFNE=Manipulate::IFNE;
        Function<Label,Manipulate> IFLT=Manipulate::IFLT;
        Function<Label,Manipulate> IFGE=Manipulate::IFGE;
        Function<Label,Manipulate> IFGT=Manipulate::IFGT;
        Function<Label,Manipulate> IFLE=Manipulate::IFLE;
        static Manipulate IFEQ(Label label) {return (v, c) -> {v.visitJumpInsn(Opcodes.IFEQ, label);return DEC1;};}
        static Manipulate IFNE(Label label) {return (v, c) -> {v.visitJumpInsn(Opcodes.IFNE, label);return DEC1;};}
        static Manipulate IFLT(Label label) {return (v, c) -> {v.visitJumpInsn(Opcodes.IFLT, label);return DEC1;};}
        static Manipulate IFGE(Label label) {return (v, c) -> {v.visitJumpInsn(Opcodes.IFGE, label);return DEC1;};}
        static Manipulate IFGT(Label label) {return (v, c) -> {v.visitJumpInsn(Opcodes.IFGT, label);return DEC1;};}
        static Manipulate IFLE(Label label) {return (v, c) -> {v.visitJumpInsn(Opcodes.IFLE, label);return DEC1;};}
        //@formatter:on
        static Object internalName(TypeDescription type) {
            if (type.isPrimitive()) {
                if (ByteCodeManipulates.INT_LIKE.isAssignableFrom(type)) return Opcodes.INTEGER;
                if (ByteCodeManipulates.FLOAT.isAssignableFrom(type)) return Opcodes.FLOAT;
                if (ByteCodeManipulates.DOUBLE.isAssignableFrom(type)) return Opcodes.DOUBLE;
                if (ByteCodeManipulates.LONG.isAssignableFrom(type)) return Opcodes.LONG;
            }
            return type.asErasure().getInternalName();
        }

        static Object[] toInternalName(List<TypeDescription> types) {
            if (types == null || types.isEmpty()) return null;
            int n = types.size();
            Object[] out = new Object[n];
            for (int i = 0; i < n; i++) {
                out[i] = internalName(types.get(i));
            }
            return out;
        }


        /**
         * declare a Label
         *
         * @param label     the label to declar
         * @param frameType the frame type of
         *                  {@link Opcodes#F_FULL},{@link Opcodes#F_APPEND},{@link Opcodes#F_CHOP},{@link Opcodes#F_SAME} or {@link Opcodes#F_SAME1} otherwise won't add Frame Map
         * @param locals    the locals match with frameType
         * @param stack     the stack match with frameType
         */
        static Manipulate LABEL(Label label, int frameType, List<TypeDescription> locals, List<TypeDescription> stack) {
            int localSize = locals == null ? 0 : locals.size();
            int stackSize = stack == null ? 0 : stack.size();
            Object[] localName = toInternalName(locals);
            Object[] stackName = toInternalName(stack);
            switch (frameType) {
                case Opcodes.F_FULL:
                    return (v, c) -> {
                        v.visitLabel(label);
                        v.visitFrame(Opcodes.F_FULL, localSize, localName, stackSize, stackName);
                        return Size.ZERO;
                    };
                case Opcodes.F_APPEND:
                    return (v, c) -> {
                        v.visitLabel(label);
                        v.visitFrame(Opcodes.F_APPEND, localSize, localName, 0, null);
                        return Size.ZERO;
                    };
                case Opcodes.F_SAME:
                    return (v, c) -> {
                        v.visitLabel(label);
                        v.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                        return Size.ZERO;
                    };
                case Opcodes.F_SAME1:
                    return (v, c) -> {
                        v.visitLabel(label);
                        v.visitFrame(Opcodes.F_SAME1, 0, null, 1, stackName);
                        return Size.ZERO;
                    };
                case Opcodes.F_CHOP:
                    return (v, c) -> {
                        v.visitLabel(label);
                        v.visitFrame(Opcodes.F_CHOP, localSize, null, 0, null);
                        return Size.ZERO;
                    };
                default:
                    return (v, c) -> {
                        v.visitLabel(label);
                        return Size.ZERO;
                    };
            }
        }

        static int handleKindDecider(ByteCodeElement target, boolean setter) {
            if (target instanceof MethodDescription) {
                MethodDescription m = (MethodDescription) target;
                return m.isStatic()
                        ? Opcodes.H_INVOKESTATIC
                        : m.isConstructor() ? Opcodes.H_NEWINVOKESPECIAL
                        : m.getDeclaringType().isInterface() ? Opcodes.H_INVOKEINTERFACE
                        : !m.isMethod() || m.isAbstract() || m.isDefaultMethod() ? Opcodes.H_INVOKESPECIAL
                        : Opcodes.H_INVOKEVIRTUAL; //TODO
            } else if (target instanceof FieldDescription) {
                FieldDescription f = (FieldDescription) target;
                return f.isStatic()
                        ? (setter ? Opcodes.H_PUTSTATIC : Opcodes.H_GETSTATIC)
                        : (setter ? Opcodes.H_PUTFIELD : Opcodes.H_GETFIELD);
            } else throw new AssertionError("unknown handle kind of " + target);

        }

        /**
         * Bootstrap a lambda target to a Method
         *
         * @param target   target method
         * @param face     the functinal interface
         * @param closures closure parameters
         */
        static Manipulate LAMBDA(MethodDescription target, TypeDescription face, TypeDescription... closures) {
            MethodDescription.InDefinedShape delegate = face.getDeclaredMethods().filter(isAbstract()).getOnly();
            String delegateSignature = delegate.getName();
            //!! (closure1;closure2)result
            String delegateDescriptor = (closures.length == 0
                    ? "()"
                    : Arrays.stream(closures).map(NamedElement.WithDescriptor::getDescriptor).collect(Collectors.joining(";", "(", ")")))
                    + delegate.getDeclaringType().getDescriptor();
            Handle bootstrapHandle = new Handle(Opcodes.H_INVOKESTATIC,
                    "java/lang/invoke/LambdaMetafactory",
                    "metafactory",
                    "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                    false);
            Type functionType = Type.getType(delegate.getDescriptor());
            Handle targetHandle = new Handle(
                    handleKindDecider(target, false),
                    target.getDeclaringType().asErasure().getInternalName(),//owner
                    target.getName(),//method name
                    target.getDescriptor(), //descriptor
                    target.getDeclaringType().isInterface()
            );
            Type targetDescriptor = Type.getType(target.getDescriptor());
            return (mv, c) -> {
                mv.visitInvokeDynamicInsn(
                        delegateSignature,//methodName(closures;...)Result
                        delegateDescriptor,
                        bootstrapHandle,
                        //** arguments for bootstrap method
                        functionType, //arguments for functional interface method
                        targetHandle, //the target method handle
                        targetDescriptor //the target method descriptor
                );
                return new Size(1, 1);
            };
        }

        /**
         * Bootstrap a lambda target to a Field
         *
         * @param target   target field
         * @param face     the functinal interface
         * @param closures closure parameters
         */
        static Manipulate LAMBDA(FieldDescription target, TypeDescription face, TypeDescription... closures) {
            MethodDescription.InDefinedShape delegate = face.getDeclaredMethods().filter(isAbstract()).getOnly();
            String delegateSignature = delegate.getName();
            //!! (closure1;closure2)result
            String delegateDescriptor = (closures.length == 0
                    ? "()"
                    : Arrays.stream(closures).map(NamedElement.WithDescriptor::getDescriptor).collect(Collectors.joining(";", "(", ")")))
                    + delegate.getDeclaringType().getDescriptor();
            Handle bootstrapHandle = new Handle(Opcodes.H_INVOKESTATIC,
                    "java/lang/invoke/LambdaMetafactory",
                    "metafactory",
                    "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                    false);
            Type functionType = Type.getType(delegate.getDescriptor());
            Handle targetHandle = new Handle(
                    handleKindDecider(target, (delegate.getParameters().size() - closures.length) != 0),//setter
                    target.getDeclaringType().asErasure().getInternalName(),//owner
                    target.getName(),//method name
                    target.getDescriptor(), //descriptor
                    target.getDeclaringType().isInterface()
            );
            Type targetDescriptor = Type.getType(target.getDescriptor());
            return (mv, c) -> {
                mv.visitInvokeDynamicInsn(
                        delegateSignature,//methodName(closures;...)Result
                        delegateDescriptor,
                        bootstrapHandle,
                        //** arguments for bootstrap method
                        functionType, //arguments for functional interface method
                        targetHandle, //the target method handle
                        targetDescriptor //the target method descriptor
                );
                return new Size(1, 1);
            };
        }
    }


    class Manual implements ByteCodeManipulates {
        final Manipulates manipulates = new Manipulates();
        final AtomicInteger local = new AtomicInteger();

        public Manual append(StackManipulation m) {
            manipulates.push(m);
            return this;
        }

        //region Operates
        //@formatter:off
        public Manual IADD() {  return append(Addition.INTEGER);}
        public Manual LADD() {  return append(Addition.LONG);}
        public Manual FADD() {  return append(Addition.FLOAT);}
        public Manual DADD() {  return append(Addition.DOUBLE);}

        public Manual ISUB() {  return append(Subtraction.INTEGER);}
        public Manual LSUB() {  return append(Subtraction.LONG);}
        public Manual FSUB() {  return append(Subtraction.FLOAT);}
        public Manual DSUB() {  return append(Subtraction.DOUBLE);}

        public Manual IMUL() {  return append(Multiplication.INTEGER);}
        public Manual LMUL() {  return append(Multiplication.LONG);}
        public Manual FMUL() {  return append(Multiplication.FLOAT);}
        public Manual DMUL() {  return append(Multiplication.DOUBLE);}

        public Manual IDIV() {  return append(Division.INTEGER);}
        public Manual LDIV() {  return append(Division.LONG);}
        public Manual FDIV() {  return append(Division.FLOAT);}
        public Manual DDIV() {  return append(Division.DOUBLE);}

        public Manual IREM() {  return append(Remainder.INTEGER);}
        public Manual LREM() {  return append(Remainder.LONG);}
        public Manual FREM() {  return append(Remainder.FLOAT);}
        public Manual DREM() {  return append(Remainder.DOUBLE);}

        public Manual INEG() {  return append(INEG);}
        public Manual LNEG() {  return append(LNEG);}
        public Manual FNEG() {  return append(FNEG);}
        public Manual DNEG() {  return append(DNEG);}

        public Manual ISHL() {  return append(ShiftLeft.INTEGER);}
        public Manual LSHL() {  return append(ShiftLeft.LONG);}

        public Manual ISHR() {  return append(ShiftRight.INTEGER);}
        public Manual LSHR() {  return append(ShiftRight.LONG);}

        public Manual IOR() {  return append(IOR);}
        public Manual LOR() {  return append(LOR);}

        public Manual IXOR() {  return append(IXOR);}
        public Manual LXOR() {  return append(LXOR);}
        public Manual IAND() {  return append(IAND);}
        public Manual LAND() {  return append(LAND);}

        public Manual LCMP() {  return append(LCMP);}
        public Manual FCMPG() {  return append(FCMPG);}
        public Manual FCMPL() {  return append(FCMPL);}
        public Manual DCMPG() {  return append(DCMPG);}
        public Manual DCMPL() {  return append(DCMPL);}

        public Manual SWAP() {  return append(SWAP);}

        public Manual POP1() {  return append(Removal.SINGLE);}
        public Manual POP2() {  return append(Removal.DOUBLE);}
        public Manual DUP() {  return append(Duplication.SINGLE);}
        public Manual DUP2() {  return append(Duplication.DOUBLE);}
        public Manual DUPX1() {  return append(Duplication.SINGLE.flipOver(INT));}
        public Manual DUPX2() {  return append(Duplication.SINGLE.flipOver(LONG));}
        public Manual DUP2X1() {  return append(Duplication.DOUBLE.flipOver(INT));}
        public Manual DUP2X2() {  return append(Duplication.DOUBLE.flipOver(LONG));}

        public Manual ALOAD(int vi) {  return append(MethodVariableAccess.REFERENCE.loadFrom(vi));}
        public Manual ILOAD(int vi) {  return append(MethodVariableAccess.INTEGER.loadFrom(vi));}
        public Manual LLOAD(int vi) {  return append(MethodVariableAccess.LONG.loadFrom(vi));}
        public Manual FLOAD(int vi) {  return append(MethodVariableAccess.FLOAT.loadFrom(vi));}
        public Manual DLOAD(int vi) {  return append(MethodVariableAccess.DOUBLE.loadFrom(vi));}
        public Manual ASTORE(int vi) {  return append(MethodVariableAccess.REFERENCE.storeAt(vi));}
        public Manual ISTORE(int vi) {  return append(MethodVariableAccess.INTEGER.storeAt(vi));}
        public Manual LSTORE(int vi) {  return append(MethodVariableAccess.LONG.storeAt(vi));}
        public Manual FSTORE(int vi) {  return append(MethodVariableAccess.FLOAT.storeAt(vi));}
        public Manual DSTORE(int vi) {  return append(MethodVariableAccess.DOUBLE.storeAt(vi));}

        public Manual ATHROW() {  return append(ATHROW);}

        public Manual IINC(int vi,int v) {  return append(Manipulate.IINC(vi,v));}

        public Manual NEW(TypeDescription type) {return append(TypeCreation.of(type));}
        public Manual CHECKCAST(TypeDescription type){return append(TypeCasting.to(type));}
        public Manual INSTANCEOF(TypeDescription type)
        {return append(InstanceCheck.of(type));}

        public Manual NEWARRAY(TypeDescription.Generic component, StackManipulation... values)
        {return append(ArrayFactory.forType(component).withValues(Arrays.asList(values)));}

        public Manual ARRAYLENGTH() {return append(ArrayLength.INSTANCE);}

        public Manual AASTORE() {return append(ArrayAccess.REFERENCE.store());}
        public Manual AALOAD() {return append(ArrayAccess.REFERENCE.load());}
        public Manual IASTORE() {return append(ArrayAccess.INTEGER.store());}
        public Manual IALOAD() {return append(ArrayAccess.INTEGER.load());}
        public Manual LASTORE() {return append(ArrayAccess.LONG.store());}
        public Manual LALOAD() {return append(ArrayAccess.LONG.load());}
        public Manual FASTORE() {return append(ArrayAccess.FLOAT.store());}
        public Manual FALOAD() {return append(ArrayAccess.FLOAT.load());}
        public Manual DASTORE() {return append(ArrayAccess.DOUBLE.store());}
        public Manual DALOAD() {return append(ArrayAccess.DOUBLE.load());}
        public Manual BASTORE() {return append(ArrayAccess.BYTE.store());}
        public Manual BALOAD() {return append(ArrayAccess.BYTE.load());}
        public Manual CASTORE() {return append(ArrayAccess.CHARACTER.store());}
        public Manual CALOAD() {return append(ArrayAccess.CHARACTER.load());}
        public Manual SASTORE() {return append(ArrayAccess.SHORT.store());}
        public Manual SALOAD() {return append(ArrayAccess.SHORT.load());}

        public Manual ARETURN() {return append(MethodReturn.REFERENCE);}
        public Manual IRETURN() {return append(MethodReturn.INTEGER);}
        public Manual LRETURN() {return append(MethodReturn.LONG);}
        public Manual FRETURN() {return append(MethodReturn.FLOAT);}
        public Manual DRETURN() {return append(MethodReturn.DOUBLE);}
        public Manual RETURN() {return append(MethodReturn.VOID);}

        public Manual GETFIELD(FieldDescription field) {return append(FieldAccess.forField(field).read());}
        public Manual PUTFIELD(FieldDescription field) {return append(FieldAccess.forField(field).write());}



        public Manual IFEQ(Label lbl){return append(IFEQ.apply(lbl));}
        public Manual IFNE(Label lbl){return append(IFNE.apply(lbl));}
        public Manual IFLT(Label lbl){return append(IFLT.apply(lbl));}
        public Manual IFGE(Label lbl){return append(IFGE.apply(lbl));}
        public Manual IFGT(Label lbl){return append(IFGT.apply(lbl));}
        public Manual IFLE(Label lbl){return append(IFLE.apply(lbl));}

        public Manual  IF_ICMPEQ(Label lbl){return append( IF_ICMPEQ.apply(lbl));}
        public Manual  IF_ICMPNE(Label lbl){return append( IF_ICMPNE.apply(lbl));}
        public Manual  IF_ICMPLT(Label lbl){return append( IF_ICMPLT.apply(lbl));}
        public Manual  IF_ICMPGE(Label lbl){return append( IF_ICMPGE.apply(lbl));}
        public Manual  IF_ICMPGT(Label lbl){return append( IF_ICMPGT.apply(lbl));}
        public Manual  IF_ICMPLE(Label lbl){return append( IF_ICMPLE.apply(lbl));}
        public Manual  IF_ACMPEQ(Label lbl){return append( IF_ACMPEQ.apply(lbl));}
        public Manual  IF_ACMPNE(Label lbl){return append( IF_ACMPNE.apply(lbl));}


        /**
         * @see Manipulate#LABEL( Label, int, List, List)
         */
        public Manual  LABEL(Label lbl,int type,List<TypeDescription> locals, List<TypeDescription> stack){return append(Manipulate.LABEL(lbl,type,locals,stack));}
        public Manual  LAMBDA(MethodDescription target, TypeDescription face, TypeDescription... closures){return append(Manipulate.LAMBDA(target, face, closures));}
        public Manual  LAMBDA(FieldDescription target, TypeDescription face, TypeDescription... closures){return append(Manipulate.LAMBDA(target, face, closures));}



        //@formatter:on
        public Manual INVOKE(MethodDescription method) {
            return append(MethodInvocation.invoke(method));
        }

        public Manual INVOKE_SPEICAL(MethodDescription method, TypeDescription target) {
            return append(MethodInvocation.invoke(method).special(target));
        }

        public Manual INVOKE_VITRUAL(MethodDescription method, TypeDescription target) {
            return append(MethodInvocation.invoke(method).virtual(target));
        }

        public Manual BOX(TypeDescription type) {
            return append(PrimitiveBoxingDelegate
                    .forPrimitive(type)
                    .assignBoxedTo(TypeDescription.Generic.OBJECT.asGenericType(), Assigner.DEFAULT, Assigner.Typing.STATIC));
        }

        public Manual UNBOX(TypeDescription type) {
            return append(PrimitiveUnboxingDelegate
                    .forReferenceType(type)
                    .assignUnboxedTo(type.asUnboxed().asGenericType(), Assigner.GENERICS_AWARE, Assigner.Typing.DYNAMIC));
        }

        //endregion

        public Manual adjustLocalSize(int size) {
            local.set(size);
            return this;
        }

        @Override
        public Size apply(MethodVisitor v, Implementation.Context c, MethodDescription m) {
            Size size = Size.ZERO.merge(new Simple(manipulates).apply(v, c, m));
            return size.merge(new Size(0, local.get() + m.getStackSize()));
        }

    }

    /**
     * use Manual Assembler to build ASM
     */
    static Manual manual(){return new Manual();}
    class Compute implements ByteCodeManipulates {
        @Value @AllArgsConstructor
        static class Frame{
            int op;
            List<TypeDescription> locals;
            List<TypeDescription> stack;
            Frame pre;


            public Frame(Compute assembler) {
                this(Integer.MIN_VALUE,
                        new ArrayList<>(assembler.locals),
                        new ArrayList<>(assembler.stack),
                        assembler.initial);
            }


            public Frame differ(Frame last) {
                if (last == null) return this;
                boolean sameLocal = last.locals.equals(locals);
                boolean sameStack = stack.equals(last.stack);
                if (sameLocal && sameStack) {
                    //SAME
                    return new Frame(Opcodes.F_SAME, Collections.emptyList(), Collections.emptyList(), last);
                }
                if (stack.isEmpty() && !sameLocal) {
                    int diff = locals.size() - last.locals.size();
                    if (diff > 0 && diff <= 3) {
                        if (locals.subList(0, locals.size() - diff).equals(last.locals)) {
                            return new Frame(Opcodes.F_APPEND, locals.subList(locals.size() - diff, locals.size()), Collections.emptyList(), last);
                        }
                    } else if (diff >= -3 && diff < 0) {
                        if (last.locals.subList(0, last.locals.size() - diff).equals(locals)) {
                            return new Frame(Opcodes.F_CHOP, last.locals.subList(last.locals.size() - diff, last.locals.size()), Collections.emptyList(), last);
                        }
                    }
                }
                if (sameLocal && stack.size() == 1) {
                    //SAME 1
                    return new Frame(Opcodes.F_SAME1, Collections.emptyList(), stack, last);
                }
                return new Frame(Opcodes.F_FULL, locals, stack, last);

            }

            @Override
            public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                Frame frame = (Frame) o;
                return op == frame.op && Objects.equals(locals, frame.locals) && Objects.equals(stack, frame.stack);
            }

            @Override
            public int hashCode() {
                return Objects.hash(op, locals, stack);
            }

            @Override
            public String toString() {
                return new StringJoiner(", ", Frame.class.getSimpleName() + "[", "]")
                        .add("op=" + (
                                   op== Opcodes.F_NEW? "F_NEW":
                                   op== Opcodes.F_FULL? "F_FULL":
                                   op== Opcodes.F_APPEND? "F_APPEND":
                                   op== Opcodes.F_CHOP? "F_CHOP":
                                   op== Opcodes.F_SAME? "F_SAME":
                                   op== Opcodes.F_SAME1? "F_SAME1":
                                   op== Integer.MAX_VALUE? "INITIAL":
                                   op== Integer.MIN_VALUE? "NONE_CALCULATED":
                                    "UNKNOWN<" + op + ">"
                                )
                        )
                        .add("locals=" + locals)
                        .add("stack=" + stack)
                        .add("pre=" + (pre == null ? "NULL" : pre == this ? "SELF" : pre))
                        .toString();
            }
        }


        final Stack<ByteCodeAppender> manipulates = new Stack<>();
        final Stack<StackManipulation> operates = new Stack<>();
        final Stack<TypeDescription> locals = new Stack<>();
        final Stack<TypeDescription> stack = new Stack<>();
        final Map<Label, Frame> frame = new HashMap<>();
        final Frame initial;
        private final TypeDescription target;
        private final MethodDescription method;
        private final int offset;

        public Compute(TypeDescription target, MethodDescription method) {
            this.target = target;
            this.method = method;
            if (method != null && !method.isStatic()) locals.push(target);
            if (method != null) method.getParameters().asTypeList().forEach(x -> locals.push(x.asErasure()));
            this.offset = (method != null ? (method.isStatic() ? 0 : 1) + (method.getParameters().size()) : 0);
            initial = new Frame(Integer.MAX_VALUE, new ArrayList<>(locals), Collections.emptyList(), null);
        }

        private static boolean notAssignable(TypeDescription a, TypeDescription b) {
            // if(a.represents(Object.class)&&b.isAssignableTo(TypeDescription.OBJECT)) return true;
            return b == INT_LIKE ? !b.isAssignableFrom(a) : !a.isAssignableTo(b);
        }

        private boolean require(boolean pop, int amount, TypeDescription... types) {
            if (stack.size() < amount) return false;
            int index = stack.size() - amount;
            if (types.length == 0) {
                return true;
            }
            boolean result = true;
            if (types.length == 1) {
                for (int i = 0; i < amount; i++) {
                    if (notAssignable(stack.get(index), types[0])) return false;
                    index++;
                }
            } else if (types.length == amount) {
                for (int i = 0; i < amount; i++) {
                    if (notAssignable(stack.get(index), types[i])) return false;
                    index++;
                }
            }
            if (pop) stack.removeElement(stack.size() - amount, stack.size());
            return result;

        }

        private void pushIn(TypeDescription type) {
            stack.push(type);
        }

        private TypeDescription popOut() {
            assert require(false, 1) : "invalid stack size for pop one";
            return stack.pop();
        }

        public Compute append(ByteCodeAppender operate) {
            manipulates.push(operate);
            return this;
        }

        private void append(StackManipulation operate) {
            operates.push(operate);
        }

        private void clearOperates() {
            if (!operates.isEmpty()) {
                append(new ByteCodeAppender.Simple(new ArrayList<>(operates)));
                operates.clear();
            }
        }

        /**
         * add a Manual fragment
         *
         * @param action the generator consumer current locals and stack returns appending ByteCodeAppender
         */
        public Compute manual(BiFunction<Stack<TypeDescription>, Stack<TypeDescription>, ByteCodeAppender> action) {
            clearOperates();
            manipulates.push(action.apply(locals, stack));
            return this;
        }

        //region Mathematical
        public Compute add() {
            if (require(true, 2, INT_LIKE)) {
                append(Addition.INTEGER);
                pushIn(INT);
            } else if (require(true, 2, LONG)) {
                append(Addition.LONG);
                pushIn(LONG);
            } else if (require(true, 2, FLOAT)) {
                append(Addition.FLOAT);
                pushIn(FLOAT);
            } else if (require(true, 2, DOUBLE)) {
                append(Addition.DOUBLE);
                pushIn(DOUBLE);
            } else throw new IllegalArgumentException("top two value on stack not match any Addition requirement");
            return this;
        }

        public Compute sub() {
            if (require(true, 2, INT_LIKE)) {
                append(Subtraction.INTEGER);
                pushIn(INT);
            } else if (require(true, 2, LONG)) {
                append(Subtraction.LONG);
                pushIn(LONG);
            } else if (require(true, 2, FLOAT)) {
                append(Subtraction.FLOAT);
                pushIn(FLOAT);
            } else if (require(true, 2, DOUBLE)) {
                append(Subtraction.DOUBLE);
                pushIn(DOUBLE);
            } else throw new IllegalArgumentException("top two value on stack not match any Subtraction requirement");
            return this;
        }

        public Compute mul() {
            if (require(true, 2, INT_LIKE)) {
                append(Multiplication.INTEGER);
                pushIn(INT);
            } else if (require(true, 2, LONG)) {
                append(Multiplication.LONG);
                pushIn(LONG);
            } else if (require(true, 2, FLOAT)) {
                append(Multiplication.FLOAT);
                pushIn(FLOAT);
            } else if (require(true, 2, DOUBLE)) {
                append(Multiplication.DOUBLE);
                pushIn(DOUBLE);
            } else throw new IllegalArgumentException("top two value on stack not match any Multiplication requirement");
            return this;
        }

        public Compute div() {
            if (require(true, 2, INT_LIKE)) {
                append(Division.INTEGER);
                pushIn(INT);
            } else if (require(true, 2, LONG)) {
                append(Division.LONG);
                pushIn(LONG);
            } else if (require(true, 2, FLOAT)) {
                append(Division.FLOAT);
                pushIn(FLOAT);
            } else if (require(true, 2, DOUBLE)) {
                append(Division.DOUBLE);
                pushIn(DOUBLE);
            } else throw new IllegalArgumentException("top two value on stack not match any Division requirement");
            return this;
        }

        public Compute rem() {
            if (require(true, 2, INT_LIKE)) {
                append(Remainder.INTEGER);
                pushIn(INT);
            } else if (require(true, 2, LONG)) {
                append(Remainder.LONG);
                pushIn(LONG);
            } else if (require(true, 2, FLOAT)) {
                append(Remainder.FLOAT);
                pushIn(FLOAT);
            } else if (require(true, 2, DOUBLE)) {
                append(Remainder.DOUBLE);
                pushIn(DOUBLE);
            } else throw new IllegalArgumentException("top two value on stack not match any Remainder requirement");
            return this;
        }

        public Compute neg() {
            if (require(true, 1, INT_NUM_LIKE)) {
                append(INEG);
                pushIn(INT);
            } else if (require(true, 1, LONG)) {
                append(LNEG);
                pushIn(LONG);
            } else if (require(true, 1, FLOAT)) {
                append(FNEG);
                pushIn(FLOAT);
            } else if (require(true, 1, DOUBLE)) {
                append(DNEG);
                pushIn(DOUBLE);
            } else throw new IllegalArgumentException("top two value on stack not match any Negative requirement");
            return this;
        }

        public Compute shl() {
            if (require(true, 2, INT_NUM_LIKE)) {
                append(ShiftLeft.INTEGER);
                pushIn(INT);
            } else if (require(true, 2, LONG)) {
                append(ShiftLeft.LONG);
                pushIn(LONG);
            } else throw new IllegalArgumentException("top two value on stack not match any ShiftLeft requirement");
            return this;
        }

        public Compute shr() {
            if (require(true, 2, INT_NUM_LIKE)) {
                append(ShiftRight.INTEGER);
                pushIn(INT);
            } else if (require(true, 2, LONG)) {
                append(ShiftRight.LONG);
                pushIn(LONG);
            } else throw new IllegalArgumentException("top two value on stack not match any ShiftRight requirement");
            return this;
        }

        public Compute or() {
            if (require(true, 2, INT_NUM_LIKE)) {
                append(IOR);
                pushIn(INT);
            } else if (require(true, 2, LONG)) {
                append(LOR);
                pushIn(LONG);
            } else throw new IllegalArgumentException("top two value on stack not match any bitwise OR requirement");
            return this;
        }

        public Compute xor() {
            if (require(true, 2, INT_NUM_LIKE)) {
                append(IXOR);
                pushIn(INT);
            } else if (require(true, 2, LONG)) {
                append(LXOR);
                pushIn(LONG);
            } else throw new IllegalArgumentException("top two value on stack not match any bitwise XOR requirement");
            return this;
        }

        public Compute and() {
            if (require(true, 2, INT_NUM_LIKE)) {
                append(IAND);
                pushIn(INT);
            } else if (require(true, 2, LONG)) {
                append(LAND);
                pushIn(LONG);
            } else throw new IllegalArgumentException("top two value on stack not match any bitwise AND requirement");
            return this;
        }


        //endregion
        //region Comparative and Jump
        public Compute cmp(boolean nanBigger) {
            if (require(true, 2, LONG)) {
                append(LCMP);
                pushIn(LONG);
            }
            if (require(true, 2, FLOAT)) {
                append(nanBigger ? FCMPG : FCMPL);
                pushIn(FLOAT);
            }
            if (require(true, 2, DOUBLE)) {
                append(nanBigger ? DCMPG : DCMPL);
                pushIn(DOUBLE);
            } else throw new IllegalArgumentException("top two value on stack not match any Comparative requirement");
            return this;
        }

        private boolean checkOld(Label label, Frame current) {
            Frame old = frame.get(label);
            if (old == null) return true;
            return old.equals(current);
        }

        private Frame calc( Label from,  Label to) {
            if (to == null && from == null) return null;
            Frame current = new Frame(this);
            if (to != null && from != null) { //forward jump
                Frame last = frame.get(from);
                assert last != null : "missing from label frame";
                Frame old = frame.get(to);
                Frame differ = current.differ(last);
                assert old == null || differ.equals(old) : "invalid Frame from last jump location: current " + differ + " vs old " + old;
                frame.put(to, differ);
                return differ;
            } else if (to != null) {//back jump
                current = current.differ(initial);
                assert checkOld(to, current) : "invalid backward jump frame: old " + frame.get(to) + " vs " + current;
                frame.put(to, current);
                return current;
            } else {
                frame.put(from, current.differ(initial));
                return current;
            }
        }

        /**
         * Calc Frame with initial frame
         */
        private Frame calc(Label to) {
            Frame current = new Frame(this);
            Frame old = frame.get(to);
            Frame differ = current.differ(initial);
            assert old == null || differ.equals(old) : "invalid Frame from last jump location: " + old + " vs " + differ;
            frame.put(to, differ);
            return differ;
        }


        //@formatter:off
        /**
         * Jump from on Frame to Other frame
         *
         * @param from from Frame label ;null for backwards jump.
         * @param to   to Frame label
         */
        public Compute jump(Label from,Label to) {calc(from,to);append(GOTO.apply(to));return this;}
        /** @see #jump( Label, Label)  **/ public Compute jz(Label from,Label to) {assert require(false, 1, INT_LIKE) : "stack required one Integer";popOut();calc(from,to);append(IFEQ.apply(to));return this;}
        /** @see #jump( Label, Label)  **/ public Compute jnz(Label from,Label to) {assert require(false, 1, INT_LIKE) : "stack required one Integer";popOut();calc(from,to);append(IFNE.apply(to));return this;}
        /** @see #jump( Label, Label)  **/ public Compute jlez(Label from,Label to) {assert require(false, 1, INT_LIKE) : "stack required one Integer";popOut();calc(from,to);append(IFLE.apply(to));return this;}
        /** @see #jump( Label, Label)  **/ public Compute jgez(Label from,Label to) {assert require(false, 1, INT_LIKE) : "stack required one Integer";popOut();calc(from,to);append(IFGE.apply(to));return this;}
        /** @see #jump( Label, Label)  **/ public Compute jltz(Label from,Label to) {assert require(false, 1, INT_LIKE) : "stack required one Integer";popOut();calc(from,to);append(IFLT.apply(to));return this;}
        /** @see #jump( Label, Label)  **/ public Compute jgtz(Label from,Label to) {assert require(false, 1, INT_LIKE) : "stack required one Integer";popOut();calc(from,to);append(IFGT.apply(to));return this;}
        /** @see #jump( Label, Label)  **/ public Compute je(Label from,Label to) {assert require(false, 2, INT_LIKE) : "stack required two Integer";popOut();popOut();calc(from,to);append(IF_ICMPEQ.apply(to));return this;}
        /** @see #jump( Label, Label)  **/ public Compute jn(Label from,Label to) {assert require(false, 2, INT_LIKE) : "stack required two Integer";popOut();popOut();calc(from,to);append(IF_ICMPNE.apply(to));return this;}
        /** @see #jump( Label, Label)  **/ public Compute jle(Label from,Label to) {assert require(false, 2, INT_LIKE) : "stack required two Integer";popOut();popOut();calc(from,to);append(IF_ICMPLE.apply(to));return this;}
        /** @see #jump( Label, Label)  **/ public Compute jge(Label from,Label to) {assert require(false, 2, INT_LIKE) : "stack required two Integer";popOut();popOut();calc(from,to);append(IF_ICMPGE.apply(to));return this;}
        /** @see #jump( Label, Label)  **/ public Compute jlt(Label from,Label to) {assert require(false, 2, INT_LIKE) : "stack required two Integer";popOut();popOut();calc(from,to);append(IF_ICMPLT.apply(to));return this;}
        /** @see #jump( Label, Label)  **/ public Compute jgt(Label from,Label to) {assert require(false, 2, INT_LIKE) : "stack required two Integer";popOut();popOut();calc(from,to);append(IF_ICMPGT.apply(to));return this;}
        /** @see #jump( Label, Label)  **/ public Compute jea(Label from,Label to) {assert require(false, 2, TypeDescription.OBJECT) : "stack required two Object";popOut();popOut();calc(from,to);append(IF_ACMPEQ.apply(to));return this;}
        /** @see #jump( Label, Label)  **/ public Compute jna(Label from,Label to) {assert require(false, 2, TypeDescription.OBJECT) : "stack required two Object";popOut();popOut();calc(from,to);append(IF_ACMPNE.apply(to));return this;}
        /** @see #jump( Label, Label)  **/ public Compute jen(Label from,Label to) {assert require(false, 1, TypeDescription.OBJECT) : "stack required one Object";popOut();popOut();calc(from,to);append(IFNULL.apply(to));return this;}
        /** @see #jump( Label, Label)  **/ public Compute jnn(Label from,Label to) {assert require(false, 1, TypeDescription.OBJECT) : "stack required one Object";popOut();popOut();calc(from,to);append(IFNONNULL.apply(to));return this;}
        /** @see #jump( Label, Label)  **/ public Compute goTo(Label from,Label to){return jump(from,to);}
        /** @see #jump( Label, Label)  **/ public Compute ifEQ(Label from,Label to){return jz(from,to);}
        /** @see #jump( Label, Label)  **/ public Compute ifNE(Label from,Label to){return jnz(from,to);}
        /** @see #jump( Label, Label)  **/ public Compute ifLE(Label from,Label to){return jlez(from,to);}
        /** @see #jump( Label, Label)  **/ public Compute ifGE(Label from,Label to){return jgez(from,to);}
        /** @see #jump( Label, Label)  **/ public Compute ifLT(Label from,Label to){return jltz(from,to);}
        /** @see #jump( Label, Label)  **/ public Compute ifGT(Label from,Label to){return jgtz(from,to);}
        /** @see #jump( Label, Label)  **/ public Compute ifCmpEQ(Label from,Label to){return je(from,to);}
        /** @see #jump( Label, Label)  **/ public Compute ifCmpNE(Label from,Label to){return jn(from,to);}
        /** @see #jump( Label, Label)  **/ public Compute ifCmpLT(Label from,Label to){return jlt(from,to);}
        /** @see #jump( Label, Label)  **/ public Compute ifCmpLE(Label from,Label to){return jle(from,to);}
        /** @see #jump( Label, Label)  **/ public Compute ifCmpGT(Label from,Label to){return jgt(from,to);}
        /** @see #jump( Label, Label)  **/ public Compute ifCmpGE(Label from,Label to){return jge(from,to);}
        /** @see #jump( Label, Label)  **/ public Compute ifAEQ(Label from,Label to){return jea(from,to);}
        /** @see #jump( Label, Label)  **/ public Compute ifANE(Label from,Label to){return jna(from,to);}
        /** @see #jump( Label, Label)  **/ public Compute ifNull(Label from,Label to){return jen(from,to);}
        /** @see #jump( Label, Label)  **/ public Compute ifNonNull(Label from,Label to){return jnn(from,to);}


        //@formatter:on

        //endregion
        //region Label

        /**
         * Manual define label and Frame Map
         *
         * @param label     the label
         * @param frameType the FrameMapType in {@link Opcodes#F_FULL} to {@link Opcodes#F_SAME1} [0,4] or value greater than 4 for not use Frame Map
         * @param local     local for frameType
         * @param stack     stack for FrameType
         */
        public Compute labelManual(Label label, int frameType, List<TypeDescription> local, List<TypeDescription> stack) {
            assert frameType >= 0 : "invalid frame type " + frameType;
            append(Manipulate.LABEL(label, frameType, local, stack));
            return this;
        }

        /**
         * declare label and compute frame against initial frame
         *
         * @param label current label
         */
        public Compute labelComputeInit(Label label) {
            Frame frm = calc(label);
            append(Manipulate.LABEL(label, frm.op, frm.locals, frm.stack));
            return this;
        }

        /**
         * declare label and compute frame against pre declared one
         *
         * @param from  predefined label
         * @param label current label
         */
        public Compute labelCompute(Label from, Label label) {
            Frame frm = calc(from, label);
            append(Manipulate.LABEL(label, frm.op, frm.locals, frm.stack));
            return this;
        }

        /**
         * declare a label and compute frame against last jump point
         *
         * @param lbl the label
         */
        public Compute label(Label lbl) {
            Frame frm = frame.remove(lbl);
            assert frm != null : "empty frame for label";
            append(Manipulate.LABEL(lbl, frm.op, frm.locals, frm.stack));
            return this;
        }

        //endregion

        //region Locals and Stack
        public Compute swap() {
            assert require(false, 2) : "empty stack for swap";
            TypeDescription t1 = popOut();
            TypeDescription t2 = popOut();
            assert t1.getStackSize() == StackSize.SINGLE && t2.getStackSize() == StackSize.SINGLE : "swap only support single size value";
            append(SWAP);
            pushIn(t1);
            pushIn(t2);
            return this;
        }

        public Compute pop() {
            assert require(false, 1) : "empty stack for pop";
            append(Removal.of(popOut()));
            return this;
        }

        public Compute dup() {
            assert require(false, 1) : "empty stack for Duplication";
            TypeDescription t = popOut();
            append(Duplication.of(t));
            pushIn(t);
            pushIn(t);
            return this;
        }

        public Compute dupFlip() {
            assert require(false, 2) : "empty stack for Duplication and FlipOver";
            TypeDescription t1 = popOut();
            TypeDescription t2 = popOut();
            append(Duplication.of(t1).flipOver(t2));
            pushIn(t1);
            pushIn(t2);
            pushIn(t1);
            return this;
        }

        public Compute loadThis() {
            append(MethodVariableAccess.loadThis());
            pushIn(target);
            return this;
        }

        public Compute loadLocal(int vi) {
            int x = vi + offset;
            assert x < locals.size() : "invalid local as offset " + vi;
            assert locals.get(x) != TOP : "invalid local as offset " + vi + " which is part of DOUBLE size value";
            append(MethodVariableAccess.of(locals.get(x)).loadFrom(x));
            pushIn(locals.get(x));
            return this;
        }

        public Compute storeLocal() {
            TypeDescription type = popOut();
            if (type.getStackSize().getSize() == 2) {
                locals.push(TOP);
            }
            locals.push(type);
            append(MethodVariableAccess.of(type).storeAt(locals.size() - 1));

            return this;
        }

        public Compute storeLocal(int vi) {
            int x = vi + offset;
            assert x < locals.size() : "invalid local as offset " + vi;
            TypeDescription type = popOut();
            {
                TypeDescription local = locals.get(x);
                assert type.isAssignableTo(local) : "incompatible local variable type: " + type + " and " + local;
            }
            append(MethodVariableAccess.of(type).storeAt(x));
            return this;
        }

        public Compute loadParameter(int vi) {
            assert method != null : "invalid without parameters";
            assert method.getParameters().size() > vi : "invalid without enough parameters";
            int x = vi + (method.isStatic() ? 0 : 1);
            assert x < locals.size() : "invalid local as offset " + vi;
            ParameterDescription param = method.getParameters().get(vi);
            append(MethodVariableAccess.load(param));
            pushIn(param.getType().asErasure());
            return this;
        }

        public Compute readField(String name) {
            assert target != null : "no target type";
            FieldDescription.InDefinedShape field = target.getDeclaredFields().filter(named(name)).getOnly();
            assert (field.isStatic() && (method == null || method.isStatic())) || (method != null && !method.isStatic()) : "invalid access field ";
            if (!field.isStatic()) {
                assert require(false, 1, target) : "require target on stack";
                popOut();
            }
            append(FieldAccess.forField(field).read());
            pushIn(field.getType().asErasure());
            return this;
        }

        public Compute readField(FieldDescription field) {
            if (!field.isStatic()) {
                assert require(false, 1, field.getDeclaringType().asErasure()) : "require target on stack";
                popOut();
            }
            append(FieldAccess.forField(field).read());
            pushIn(field.getType().asErasure());
            return this;
        }

        public Compute writeField(FieldDescription field) {
            assert require(false, 1, field.getType().asErasure()) : "require value with match type on stack";
            popOut();//value
            if (!field.isStatic()) {
                assert require(false, 1, field.getDeclaringType().asErasure()) : "require target on stack";
                popOut(); //instance
            }
            append(FieldAccess.forField(field).write());
            return this;
        }

        public Compute writeField(String name) {
            assert target != null : "no target type";
            FieldDescription.InDefinedShape field = target.getDeclaredFields().filter(named(name)).getOnly();
            assert (field.isStatic() && (method == null || method.isStatic())) || (method != null && !method.isStatic()) : "invalid access field ";
            assert require(false, 1, field.getType().asErasure()) : "require value with match type on stack";
            popOut();//value
            if (!field.isStatic()) {
                assert require(false, 1, target) : "require target on stack";
                popOut();
            }
            append(FieldAccess.forField(field).write());
            return this;
        }

        //endregion
        //region Type
        public Compute creation(TypeDescription type) {
            append(TypeCreation.of(type));
            pushIn(type);
            return this;
        }

        public Compute cast(TypeDescription type, boolean... unsafe) {
            assert require(false, 1) : "cast on empty stack";
            TypeDescription t = popOut();
            assert unsafe.length != 0 || t.isAssignableTo(type) : "cast unsafe";
            append(TypeCasting.to(type));
            pushIn(type);
            return this;
        }

        public Compute box() {
            assert require(false, 1) : "box on empty stack";
            TypeDescription t = popOut();
            assert t.isPrimitive() : "none primitive type for box";
            assert !t.isAssignableTo(void.class) : "void type for unbox";
            append(PrimitiveBoxingDelegate.forPrimitive(t).assignBoxedTo(
                    TypeDescription.Generic.OBJECT.asGenericType(),
                    Assigner.DEFAULT,
                    Assigner.Typing.STATIC
            ));
            pushIn(t.asBoxed());
            return this;
        }

        public Compute unbox() {
            assert require(false, 1) : "unbox on empty stack";
            TypeDescription t = popOut();
            assert !t.isPrimitive() : "primitive type for unbox";
            assert !t.isAssignableTo(Void.class) : "void type for unbox";
            append(PrimitiveUnboxingDelegate.forReferenceType(t)
                    .assignUnboxedTo(t.asUnboxed().asGenericType(),
                            Assigner.GENERICS_AWARE,
                            Assigner.Typing.DYNAMIC));
            pushIn(t.asUnboxed());
            return this;
        }

        public Compute instanceOf(TypeDescription type) {
            assert require(false, 1) : "instanceOf on empty stack";
            TypeDescription t = popOut();
            assert !t.isPrimitive() : "primitive type for instanceOf";
            assert !t.isAssignableTo(Void.class) : "void type for instanceOf";
            append(InstanceCheck.of(type));
            pushIn(INT);
            return this;
        }

        //endregion
        //region Array

        public Compute array(TypeDescription.Generic component, StackManipulation... values) {
            append(ArrayFactory.forType(component).withValues(Arrays.asList(values)));
            pushIn(TypeDescription.ArrayProjection.of(component.asErasure()));
            return this;
        }

        public Compute array(TypeDescription.Generic component, List<StackManipulation> values) {
            append(ArrayFactory.forType(component).withValues(values));
            pushIn(TypeDescription.ArrayProjection.of(component.asErasure()));
            return this;
        }

        public Compute arraySet() {
            assert require(false, 3) : "operand not 3 for array set";
            TypeDescription val = popOut();
            TypeDescription idx = popOut();
            TypeDescription ar = popOut();
            assert INT_NUM_LIKE.isAssignableTo(idx) : "invalid index type " + idx;
            assert ar.isArray() : "invalid array type " + ar;
            assert Objects.requireNonNull(ar.getComponentType()).isAssignableFrom(val) : "invalid value type " + val;
            append(ArrayAccess.of(ar).store());
            return this;
        }

        public Compute arrayGet() {
            assert require(false, 2) : "operand not 2 for array get";
            TypeDescription idx = popOut();
            TypeDescription ar = popOut();
            assert INT_NUM_LIKE.isAssignableTo(idx) : "invalid index type " + idx;
            assert ar.isArray() : "invalid array type " + ar;
            append(ArrayAccess.of(ar).load());
            pushIn(ar.getComponentType());
            return this;
        }

        public Compute arrayLength() {
            assert require(false, 1) : "operand lesser than 1 for array length";
            TypeDescription ar = popOut();
            assert ar.isArray() : "invalid array type " + ar;
            append(ArrayLength.INSTANCE);
            pushIn(ar.getComponentType());
            return this;
        }

        //endregion
        //region Terminate
        public Compute aThrow() {
            assert require(false, 1, TypeDescription.THROWABLE) : "invalid stack to aThrow";
            append(ATHROW);
            return this;
        }

        public Compute returns() {
            assert method != null : "not method";
            if (method.getReturnType().asErasure().isAssignableTo(void.class)) {
                append(MethodReturn.VOID);
                return this;
            }
            assert require(false, 1) : "not value to return";
            TypeDescription t = popOut();
            assert t.isAssignableTo(method.getReturnType().asErasure()) : "invalid return type " + t;
            append(MethodReturn.of(t));
            return this;
        }


        //endregion

        //region Constant
        public Compute constNull(TypeDescription type) {
            append(NullConstant.INSTANCE);
            pushIn(type);
            return this;
        }

        public Compute constant(String text) {
            append(new TextConstant(text));
            pushIn(TypeDescription.STRING);
            return this;
        }

        //@formatter:off
        public Compute defaultValue(TypeDescription type) {append(DefaultValue.of(type));pushIn(type);return this;}
        public Compute constant(int val) {append(IntegerConstant.forValue(val));pushIn(INT);return this;}
        public Compute constant(char val) {append(IntegerConstant.forValue(val));pushIn(CHAR);return this;}
        public Compute constant(byte val) {append(IntegerConstant.forValue(val));pushIn(BYTE);return this;}
        public Compute constant(short val) {append(IntegerConstant.forValue(val));pushIn(SHORT);return this;}
        public Compute constant(boolean val) {append(IntegerConstant.forValue(val));pushIn(BOOL);return this;}
        public Compute constant(long val) {append(LongConstant.forValue(val));pushIn(LONG);return this;}
        public Compute constant(float val) {append(FloatConstant.forValue(val));pushIn(FLOAT);return this;}
        public Compute constant(double val) {append(DoubleConstant.forValue(val));pushIn(DOUBLE);return this;}
        //@formatter:on

        //endregion
        //region Invocation
        public Compute invoke(MethodDescription m) {
            checkMethodParameters(m);
            TypeDefinition t = m.getDeclaringType();
            if (!m.isStatic() && m.isMethod()) {
                assert require(false, 1, m.getDeclaringType().asErasure()) : "instance type not match";
                t = popOut();
            }
            if (!m.isMethod() || m.isAbstract())
                append(MethodInvocation.invoke(m).special(m.getDeclaringType().asErasure()));
            else if (t.getDeclaredMethods().filter(x -> x.asTypeToken().equals(m.asTypeToken())).size() < 1)
                append(MethodInvocation.invoke(m).virtual(m.getDeclaringType().asErasure()));
            else append(MethodInvocation.invoke(m));
            if (!m.getReturnType().asErasure().represents(void.class)) pushIn(m.getReturnType().asErasure());
            return this;
        }

        private void checkMethodParameters(MethodDescription m) {
            int n = m.getParameters().size();
            if (n > 0) {
                assert require(false, n) : "required parameter " + n;
                for (int i = n - 1; i >= 0; i--) {
                    TypeDescription t = popOut();
                    assert t.isAssignableTo(m.getParameters().get(i).getType().asErasure()) : "parameter type not match at " + i;
                }
            }
        }

        /**
         * Invoke Special or Invoke Virtual on Target
         *
         * @param m      method to invoke
         * @param target the target to use
         */
        public Compute invoke(MethodDescription m, TypeDescription target) {
            checkMethodParameters(m);
            if (!m.isStatic() && m.isMethod()) {
                assert require(false, 1, target) : "instance type not match";
                popOut();
            }
            if (!m.isMethod() || m.isAbstract() || m.isDefaultMethod())
                append(MethodInvocation.invoke(m).special(target));
            else append(MethodInvocation.invoke(m).virtual(m.getDeclaringType().asErasure()));
            if (!m.getReturnType().asErasure().represents(void.class)) pushIn(m.getReturnType().asErasure());
            return this;
        }

        //endregion
        //region Lambda
        //todo replace with InvokeDynamic ?
        public Compute lambda(MethodDescription method, TypeDescription face) {
            MethodDescription.InDefinedShape delegate = face.getDeclaredMethods().filter(isAbstract()).getOnly();
            //MethodInvocation.invoke(delegate).dynamic(method.getName(),method.getReturnType().asErasure(),Arrays.asList(method.getDeclaringType().asErasure()),null);
            ParameterList<ParameterDescription.InDefinedShape> target = delegate.getParameters();
            ParameterList<?> source = method.getParameters();
            if (target.size() == source.size()) { //!! same parameters
                if (method.isStatic()) {
                    //!! static method reference
                    append(Manipulate.LAMBDA(method, face));
                } else {
                    //!! instance method reference
                    assert require(false, 1) : "absent instance on stack";
                    TypeDescription t = popOut();
                    assert t.isAssignableTo(method.getDeclaringType().asErasure()) : "invalid type for instance";
                    append(Manipulate.LAMBDA(method, face, t));
                }
            } else {
                //closure size
                int clo = source.size() - target.size() + (method.isStatic() ? 0 : 1);
                TypeDescription[] closure = new TypeDescription[clo];
                TypeList types = target.asTypeList().asErasures();
                assert require(false, clo) : "absent closure parameters";
                for (int i = 0; i < clo; i++) {
                    closure[i] = popOut();
                    assert closure[i].isAssignableTo(types.get(i)) : "invalid type for closure: index " + i;
                }
                append(Manipulate.LAMBDA(method, face, closure));
            }
            pushIn(face);
            return this;

        }

        public Compute lambda(FieldDescription field, TypeDescription face) {
            MethodDescription.InDefinedShape delegate = face.getDeclaredMethods().filter(isAbstract()).getOnly();
            ParameterList<ParameterDescription.InDefinedShape> target = delegate.getParameters();
            TypeDescription.Generic source = field.getType();
            assert target.size() == 1 : "functional parameter not match for field";
            if (!field.isStatic()) {
                assert require(false, 1) : "absent instance on stack";
                TypeDescription t = popOut();
                assert t.isAssignableTo(field.getDeclaringType().asErasure()) : "invalid type for instance";
                append(Manipulate.LAMBDA(field, face, t));
            } else {
                append(Manipulate.LAMBDA(field, face));
            }
            pushIn(face);
            return this;
        }
        //endregion

        @Override
        public  Size apply(MethodVisitor v, Implementation.Context c, MethodDescription m) {
            clearOperates();
            frame.clear();
            Size size = Size.ZERO;
            for (ByteCodeAppender mani : manipulates) {
                size = size.merge(mani.apply(v, c, m));
            }
            return size.merge(new Size(0, locals.size()));
        }

    }

    /**
     * Use Compute Assembler to build ASM.</br>
     * This should slower than {@link Manual},but easier for frame compute and less error.
     * @param target the type own this ASM code
     * @param method the method own this ASM code (null if not a Method)
     */
    static Compute compute(TypeDescription target, MethodDescription method){return new Compute(target, method);}
}
