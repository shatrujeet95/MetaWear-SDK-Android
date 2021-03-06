/*
 * Copyright 2014-2015 MbientLab Inc. All rights reserved.
 *
 * IMPORTANT: Your use of this Software is limited to those specific rights granted under the terms of a software
 * license agreement between the user who downloaded the software, his/her employer (which must be your
 * employer) and MbientLab Inc, (the "License").  You may not use this Software unless you agree to abide by the
 * terms of the License which can be found at www.mbientlab.com/terms.  The License limits your use, and you
 * acknowledge, that the Software may be modified, copied, and distributed when used in conjunction with an
 * MbientLab Inc, product.  Other than for the foregoing purpose, you may not use, reproduce, copy, prepare
 * derivative works of, modify, distribute, perform, display or sell this Software and/or its documentation for any
 * purpose.
 *
 * YOU FURTHER ACKNOWLEDGE AND AGREE THAT THE SOFTWARE AND DOCUMENTATION ARE PROVIDED "AS IS" WITHOUT WARRANTY
 * OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION, ANY WARRANTY OF MERCHANTABILITY, TITLE,
 * NON-INFRINGEMENT AND FITNESS FOR A PARTICULAR PURPOSE. IN NO EVENT SHALL MBIENTLAB OR ITS LICENSORS BE LIABLE OR
 * OBLIGATED UNDER CONTRACT, NEGLIGENCE, STRICT LIABILITY, CONTRIBUTION, BREACH OF WARRANTY, OR OTHER LEGAL EQUITABLE
 * THEORY ANY DIRECT OR INDIRECT DAMAGES OR EXPENSES INCLUDING BUT NOT LIMITED TO ANY INCIDENTAL, SPECIAL, INDIRECT,
 * PUNITIVE OR CONSEQUENTIAL DAMAGES, LOST PROFITS OR LOST DATA, COST OF PROCUREMENT OF SUBSTITUTE GOODS, TECHNOLOGY,
 * SERVICES, OR ANY CLAIMS BY THIRD PARTIES (INCLUDING BUT NOT LIMITED TO ANY DEFENSE THEREOF), OR OTHER SIMILAR COSTS.
 *
 * Should you have any questions regarding your right to use this Software, contact MbientLab via email:
 * hello@mbientlab.com.
 */

package com.mbientlab.metawear.impl;

import com.mbientlab.metawear.AsyncDataProducer;
import com.mbientlab.metawear.Data;
import com.mbientlab.metawear.Route;
import com.mbientlab.metawear.builder.RouteBuilder;
import com.mbientlab.metawear.data.Acceleration;
import com.mbientlab.metawear.data.EulerAngles;
import com.mbientlab.metawear.data.Quaternion;
import com.mbientlab.metawear.module.Accelerometer;
import com.mbientlab.metawear.module.AccelerometerBosch;
import com.mbientlab.metawear.module.GyroBmi160;
import com.mbientlab.metawear.module.MagnetometerBmm150;
import com.mbientlab.metawear.module.SensorFusionBosch;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Calendar;

import bolts.Task;

import static com.mbientlab.metawear.impl.Constant.Module.SENSOR_FUSION;

/**
 * Created by etsai on 11/12/16.
 */
class SensorFusionBoschImpl extends ModuleImplBase implements SensorFusionBosch {
    private static final long serialVersionUID = -7041546136871081754L;

    private static final byte ENABLE = 1, MODE = 2, OUTPUT_ENABLE = 3,
            CORRECTED_ACC = 4, CORRECTED_ROT = 5, CORRECTED_MAG = 6,
            QUATERNION = 7, EULER_ANGLES = 8, GRAVITY_VECTOR = 9, LINEAR_ACC = 0xa;
    private final static String QUATERNION_PRODUCER= "com.mbientlab.metawear.impl.SensorFusionBoschImpl.QUATERNION_PRODUCER",
            EULER_ANGLES_PRODUCER= "com.mbientlab.metawear.impl.SensorFusionBoschImpl.EULER_ANGLES_PRODUCER",
            GRAVITY_PRODUCER= "com.mbientlab.metawear.impl.SensorFusionBoschImpl.GRAVITY_PRODUCER",
            LINEAR_ACC_PRODUCER= "com.mbientlab.metawear.impl.SensorFusionBoschImpl.LINEAR_ACC_PRODUCER",
            CORRECTED_ACC_PRODUCER= "com.mbientlab.metawear.impl.SensorFusionBoschImpl.CORRECTED_ACC_PRODUCER",
            CORRECTED_ROT_PRODUCER = "com.mbientlab.metawear.impl.SensorFusionBoschImpl.CORRECTED_ROT_PRODUCER",
            CORRECTED_MAG_PRODUCER= "com.mbientlab.metawear.impl.SensorFusionBoschImpl.CORRECTED_MAG_PRODUCER";

    private static class EulerAngleData extends DataTypeBase {
        private static final long serialVersionUID = -6075303156876049564L;

        EulerAngleData() {
            super(SENSOR_FUSION, EULER_ANGLES, new DataAttributes(new byte[] {4, 4, 4, 4}, (byte) 1, (byte) 0, true));
        }

        EulerAngleData(DataTypeBase input, Constant.Module module, byte register, byte id, DataAttributes attributes) {
            super(input, module, register, id, attributes);
        }

        @Override
        public DataTypeBase copy(DataTypeBase input, Constant.Module module, byte register, byte id, DataAttributes attributes) {
            return new EulerAngleData(input, module, register, id, attributes);
        }

        @Override
        public Number convertToFirmwareUnits(MetaWearBoardPrivate mwPrivate, Number value) {
            return value;
        }

        @Override
        public Data createMessage(boolean logData, MetaWearBoardPrivate mwPrivate, final byte[] data, final Calendar timestamp) {
            ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
            final float[] values = new float[] {buffer.getFloat(), buffer.getFloat(), buffer.getFloat(), buffer.getFloat()};

            return new DataPrivate(timestamp, data) {
                @Override
                public <T> T value(Class<T> clazz) {
                    if (clazz.equals(EulerAngles.class)) {
                        return clazz.cast(new EulerAngles(values[0], values[1], values[2], values[3]));
                    } else if (clazz.equals(float[].class)) {
                        return clazz.cast(values);
                    }
                    return super.value(clazz);
                }

                @Override
                public Class<?>[] types() {
                    return new Class<?>[] {EulerAngles.class, float[].class};
                }
            };
        }
    }
    private static class QuaternionData extends DataTypeBase {
        private static final long serialVersionUID = 6195255409423179938L;

        QuaternionData() {
            super(SENSOR_FUSION, QUATERNION, new DataAttributes(new byte[] {4, 4, 4, 4}, (byte) 1, (byte) 0, true));
        }

        QuaternionData(DataTypeBase input, Constant.Module module, byte register, byte id, DataAttributes attributes) {
            super(input, module, register, id, attributes);
        }

        @Override
        public DataTypeBase copy(DataTypeBase input, Constant.Module module, byte register, byte id, DataAttributes attributes) {
            return new QuaternionData(input, module, register, id, attributes);
        }

        @Override
        public Number convertToFirmwareUnits(MetaWearBoardPrivate mwPrivate, Number value) {
            return value;
        }

        @Override
        public Data createMessage(boolean logData, MetaWearBoardPrivate mwPrivate, final byte[] data, final Calendar timestamp) {
            ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
            final float[] values = new float[] {buffer.getFloat(), buffer.getFloat(), buffer.getFloat(), buffer.getFloat()};

            return new DataPrivate(timestamp, data) {
                @Override
                public <T> T value(Class<T> clazz) {
                    if (clazz.equals(Quaternion.class)) {
                        return clazz.cast(new Quaternion(values[0], values[1], values[2], values[3]));
                    } else if (clazz.equals(float[].class)) {
                        return clazz.cast(values);
                    }
                    return super.value(clazz);
                }

                @Override
                public Class<?>[] types() {
                    return new Class<?>[] {Quaternion.class, float[].class};
                }
            };
        }
    }
    private static class AccelerationData extends DataTypeBase {
        private static float MSS_TO_G = 9.80665f;
        private static final long serialVersionUID = -8031176383111665723L;

        AccelerationData(byte register) {
            super(SENSOR_FUSION, register, new DataAttributes(new byte[] {4, 4, 4}, (byte) 1, (byte) 0, true));
        }

        AccelerationData(DataTypeBase input, Constant.Module module, byte register, byte id, DataAttributes attributes) {
            super(input, module, register, id, attributes);
        }

        @Override
        public DataTypeBase copy(DataTypeBase input, Constant.Module module, byte register, byte id, DataAttributes attributes) {
            return new AccelerationData(input, module, register, id, attributes);
        }

        @Override
        public Number convertToFirmwareUnits(MetaWearBoardPrivate mwPrivate, Number value) {
            return value;
        }

        @Override
        public Data createMessage(boolean logData, MetaWearBoardPrivate mwPrivate, final byte[] data, final Calendar timestamp) {
            ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
            final float[] values = new float[] {buffer.getFloat() / MSS_TO_G, buffer.getFloat() / MSS_TO_G, buffer.getFloat() / MSS_TO_G};

            return new DataPrivate(timestamp, data) {
                @Override
                public <T> T value(Class<T> clazz) {
                    if (clazz.equals(Acceleration.class)) {
                        return clazz.cast(new Acceleration(values[0], values[1], values[2]));
                    } else if (clazz.equals(float[].class)) {
                        return clazz.cast(values);
                    }
                    return super.value(clazz);
                }

                @Override
                public Class<?>[] types() {
                    return new Class<?>[] {Acceleration.class, float[].class};
                }
            };
        }
    }
    private static abstract class CorrectedSensorData extends DataTypeBase {
        private static final long serialVersionUID = 4969612048732990974L;

        CorrectedSensorData(byte register) {
            super(SENSOR_FUSION, register, new DataAttributes(new byte[] {4, 4, 4, 1}, (byte) 1, (byte) 0, true));
        }

        CorrectedSensorData(DataTypeBase input, Constant.Module module, byte register, byte id, DataAttributes attributes) {
            super(input, module, register, id, attributes);
        }

        @Override
        public Number convertToFirmwareUnits(MetaWearBoardPrivate mwPrivate, Number value) {
            return value;
        }
    }
    private static class CorrectedAccelerationData extends CorrectedSensorData {
        private static final long serialVersionUID = -8672354284491044809L;

        CorrectedAccelerationData() {
            super(CORRECTED_ACC);
        }

        CorrectedAccelerationData(DataTypeBase input, Constant.Module module, byte register, byte id, DataAttributes attributes) {
            super(input, module, register, id, attributes);
        }

        @Override
        public DataTypeBase copy(DataTypeBase input, Constant.Module module, byte register, byte id, DataAttributes attributes) {
            return new CorrectedAccelerationData(input, module, register, id, attributes);
        }

        @Override
        public Data createMessage(boolean logData, MetaWearBoardPrivate mwPrivate, final byte[] data, final Calendar timestamp) {
            ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
            final CorrectedAcceleration value = new CorrectedAcceleration(buffer.getFloat() / 1000f, buffer.getFloat() / 1000f, buffer.getFloat() / 1000f, buffer.get());

            return new DataPrivate(timestamp, data) {
                @Override
                public <T> T value(Class<T> clazz) {
                    if (clazz.equals(CorrectedAcceleration.class)) {
                        return clazz.cast(value);
                    }
                    return super.value(clazz);
                }

                @Override
                public Class<?>[] types() {
                    return new Class<?>[] {CorrectedAcceleration.class};
                }
            };
        }
    }
    private static class CorrectedAngularVelocityData extends CorrectedSensorData {
        private static final long serialVersionUID = 5950000481773321231L;

        CorrectedAngularVelocityData() {
            super(CORRECTED_ROT);
        }

        CorrectedAngularVelocityData(DataTypeBase input, Constant.Module module, byte register, byte id, DataAttributes attributes) {
            super(input, module, register, id, attributes);
        }

        @Override
        public DataTypeBase copy(DataTypeBase input, Constant.Module module, byte register, byte id, DataAttributes attributes) {
            return new CorrectedAngularVelocityData(input, module, register, id, attributes);
        }

        @Override
        public Data createMessage(boolean logData, MetaWearBoardPrivate mwPrivate, final byte[] data, final Calendar timestamp) {
            ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
            final CorrectedAngularVelocity value = new CorrectedAngularVelocity(buffer.getFloat(), buffer.getFloat(), buffer.getFloat(), buffer.get());

            return new DataPrivate(timestamp, data) {
                @Override
                public <T> T value(Class<T> clazz) {
                    if (clazz.equals(CorrectedAngularVelocity.class)) {
                        return clazz.cast(value);
                    }
                    return super.value(clazz);
                }

                @Override
                public Class<?>[] types() {
                    return new Class<?>[] {CorrectedAngularVelocity.class};
                }
            };
        }
    }
    private static class CorrectedMagneticFieldData extends CorrectedSensorData {
        private static final long serialVersionUID = 5950000481773321231L;

        CorrectedMagneticFieldData() {
            super(CORRECTED_MAG);
        }

        CorrectedMagneticFieldData(DataTypeBase input, Constant.Module module, byte register, byte id, DataAttributes attributes) {
            super(input, module, register, id, attributes);
        }

        @Override
        public DataTypeBase copy(DataTypeBase input, Constant.Module module, byte register, byte id, DataAttributes attributes) {
            return new CorrectedMagneticFieldData(input, module, register, id, attributes);
        }

        @Override
        public Data createMessage(boolean logData, MetaWearBoardPrivate mwPrivate, final byte[] data, final Calendar timestamp) {
            ByteBuffer buffer = ByteBuffer.wrap(data).order(ByteOrder.LITTLE_ENDIAN);
            final CorrectedMagneticField value = new CorrectedMagneticField(buffer.getFloat() / 1000000f, buffer.getFloat() / 1000000f, buffer.getFloat() / 1000000f, buffer.get());

            return new DataPrivate(timestamp, data) {
                @Override
                public <T> T value(Class<T> clazz) {
                    if (clazz.equals(CorrectedMagneticField.class)) {
                        return clazz.cast(value);
                    }
                    return super.value(clazz);
                }

                @Override
                public Class<?>[] types() {
                    return new Class<?>[] {CorrectedMagneticField.class};
                }
            };
        }
    }

    private class SensorFusionAsyncDataProducer implements AsyncDataProducer {
        final String producerTag;
        final byte mask;

        SensorFusionAsyncDataProducer(String producerTag, byte mask) {
            this.producerTag = producerTag;
            this.mask = mask;
        }

        @Override
        public Task<Route> addRouteAsync(RouteBuilder builder) {
            return mwPrivate.queueRouteBuilder(builder, producerTag);
        }

        @Override
        public String name() {
            return producerTag;
        }

        @Override
        public void start() {
            dataEnableMask |= mask;
        }

        @Override
        public void stop() {
            dataEnableMask &= ~mask;
        }
    }

    private Mode mode;
    private byte dataEnableMask;

    SensorFusionBoschImpl(MetaWearBoardPrivate mwPrivate) {
        super(mwPrivate);

        mwPrivate.tagProducer(CORRECTED_ACC_PRODUCER, new CorrectedAccelerationData());
        mwPrivate.tagProducer(CORRECTED_ROT_PRODUCER, new CorrectedAngularVelocityData());
        mwPrivate.tagProducer(CORRECTED_MAG_PRODUCER, new CorrectedMagneticFieldData());
        mwPrivate.tagProducer(QUATERNION_PRODUCER, new QuaternionData());
        mwPrivate.tagProducer(EULER_ANGLES_PRODUCER, new EulerAngleData());
        mwPrivate.tagProducer(GRAVITY_PRODUCER, new AccelerationData(GRAVITY_VECTOR));
        mwPrivate.tagProducer(LINEAR_ACC_PRODUCER, new AccelerationData(LINEAR_ACC));
    }

    @Override
    public ConfigEditor configure() {
        return new ConfigEditor() {
            private Mode newMode = Mode.SLEEP;
            private AccRange newAccRange = AccRange.AR_16G;
            private GyroRange newGyroRange = GyroRange.GR_2000DPS;

            @Override
            public ConfigEditor mode(Mode mode) {
                newMode = mode;
                return this;
            }

            @Override
            public ConfigEditor accRange(AccRange range) {
                newAccRange = range;
                return this;
            }

            @Override
            public ConfigEditor gyroRange(GyroRange range) {
                newGyroRange = range;
                return this;
            }

            @Override
            public void commit() {
                SensorFusionBoschImpl.this.mode = newMode;
                mwPrivate.sendCommand(new byte[] {SENSOR_FUSION.id, MODE, (byte) newMode.ordinal(),
                        (byte) (newAccRange.ordinal() | ((newGyroRange.ordinal() + 1) << 4))
                });

                Accelerometer acc = (Accelerometer) mwPrivate.getModules().get(Accelerometer.class);
                GyroBmi160 gyro = (GyroBmi160) mwPrivate.getModules().get(GyroBmi160.class);
                MagnetometerBmm150 mag = (MagnetometerBmm150) mwPrivate.getModules().get(MagnetometerBmm150.class);

                switch(newMode) {
                    case SLEEP:
                        break;
                    case NDOF:
                        acc.configure()
                                .odr(100f)
                                .range(AccelerometerBosch.AccRange.values()[newAccRange.ordinal()].range)
                                .commit();
                        gyro.configure()
                                .odr(GyroBmi160.OutputDataRate.ODR_100_HZ)
                                .range(GyroBmi160.Range.values()[newGyroRange.ordinal()])
                                .commit();
                        mag.configure()
                                .outputDataRate(MagnetometerBmm150.OutputDataRate.ODR_25_HZ)
                                .commit();
                        break;
                    case IMU_PLUS:
                        acc.configure()
                                .odr(100f)
                                .range(AccelerometerBosch.AccRange.values()[newAccRange.ordinal()].range)
                                .commit();
                        gyro.configure()
                                .odr(GyroBmi160.OutputDataRate.ODR_100_HZ)
                                .range(GyroBmi160.Range.values()[newGyroRange.ordinal()])
                                .commit();
                        break;
                    case COMPASS:
                        acc.configure()
                                .odr(25f)
                                .range(AccelerometerBosch.AccRange.values()[newAccRange.ordinal()].range)
                                .commit();
                        mag.configure()
                                .outputDataRate(MagnetometerBmm150.OutputDataRate.ODR_25_HZ)
                                .commit();
                        break;
                    case M4G:
                        acc.configure()
                                .odr(50f)
                                .range(AccelerometerBosch.AccRange.values()[newAccRange.ordinal()].range)
                                .commit();
                        mag.configure()
                                .outputDataRate(MagnetometerBmm150.OutputDataRate.ODR_25_HZ)
                                .commit();
                        break;
                }
            }
        };
    }

    @Override
    public AsyncDataProducer correctedAcceleration() {
        return new SensorFusionAsyncDataProducer(CORRECTED_ACC_PRODUCER, (byte) 0x01);
    }

    @Override
    public AsyncDataProducer correctedAngularVelocity() {
        return new SensorFusionAsyncDataProducer(CORRECTED_ROT_PRODUCER, (byte) 0x02);
    }

    @Override
    public AsyncDataProducer correctedMagneticField() {
        return new SensorFusionAsyncDataProducer(CORRECTED_MAG_PRODUCER, (byte) 0x04);
    }

    @Override
    public AsyncDataProducer quaternion() {
        return new SensorFusionAsyncDataProducer(QUATERNION_PRODUCER, (byte) 0x08);
    }

    @Override
    public AsyncDataProducer eulerAngles() {
        return new SensorFusionAsyncDataProducer(EULER_ANGLES_PRODUCER, (byte) 0x10);
    }

    @Override
    public AsyncDataProducer gravity() {
        return new SensorFusionAsyncDataProducer(GRAVITY_PRODUCER, (byte) 0x20);
    }

    @Override
    public AsyncDataProducer linearAcceleration() {
        return new SensorFusionAsyncDataProducer(LINEAR_ACC_PRODUCER, (byte) 0x40);
    }

    @Override
    public void start() {
        Accelerometer acc = (Accelerometer) mwPrivate.getModules().get(Accelerometer.class);
        GyroBmi160 gyro = (GyroBmi160) mwPrivate.getModules().get(GyroBmi160.class);
        MagnetometerBmm150 mag = (MagnetometerBmm150) mwPrivate.getModules().get(MagnetometerBmm150.class);

        switch(mode) {
            case SLEEP:
                break;
            case NDOF:
                acc.acceleration().start();
                gyro.angularVelocity().start();
                mag.magneticField().start();
                acc.start();
                gyro.start();
                mag.start();
                break;
            case IMU_PLUS:
                acc.acceleration().start();
                gyro.angularVelocity().start();
                acc.start();
                gyro.start();
                break;
            case COMPASS:
            case M4G:
                acc.acceleration().start();
                mag.magneticField().start();
                acc.start();
                mag.start();
                break;
        }

        mwPrivate.sendCommand(new byte[] {SENSOR_FUSION.id, OUTPUT_ENABLE, dataEnableMask, 0x00});
        mwPrivate.sendCommand(new byte[] {SENSOR_FUSION.id, ENABLE, 0x1});
    }

    @Override
    public void stop() {
        Accelerometer acc = (Accelerometer) mwPrivate.getModules().get(Accelerometer.class);
        GyroBmi160 gyro = (GyroBmi160) mwPrivate.getModules().get(GyroBmi160.class);
        MagnetometerBmm150 mag = (MagnetometerBmm150) mwPrivate.getModules().get(MagnetometerBmm150.class);

        mwPrivate.sendCommand(new byte[] {SENSOR_FUSION.id, ENABLE, 0x0});
        mwPrivate.sendCommand(new byte[] {SENSOR_FUSION.id, OUTPUT_ENABLE, 0x00, (byte) 0x7f});

        switch(mode) {
            case SLEEP:
                break;
            case NDOF:
                acc.stop();
                gyro.stop();
                mag.stop();
                acc.acceleration().stop();
                gyro.angularVelocity().stop();
                mag.magneticField().stop();
                break;
            case IMU_PLUS:
                acc.stop();
                gyro.stop();
                acc.acceleration().stop();
                gyro.angularVelocity().stop();
                break;
            case COMPASS:
            case M4G:
                acc.stop();
                mag.stop();
                acc.acceleration().stop();
                mag.magneticField().stop();
                break;
        }
    }
}
