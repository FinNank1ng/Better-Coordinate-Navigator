package io.github.FinNank1ng.better_coordinate_navigator.client;


public class ClientConfig {

    public int configVersion = 1;

    public HUD HUD = new HUD();

    public WorldMarker WorldMarker = new WorldMarker();



    /**
     * 配置合法性检查
     */
    // 没人闲着想把配置版本搞掉吧?就你！屏幕前的，改了让你飞起来！！！焯！
    public void validate(){
        if(configVersion <= 0){
            configVersion = 1;
        }

        if(HUD == null){

            HUD = new HUD();

        }


        if(WorldMarker == null){

            WorldMarker = new WorldMarker();

        }


        HUD.validate();

        WorldMarker.validate();

    }




    /*
     * HUD配置
     */
    public static class HUD {


        /**
         * 是否显示图标
         */
        public boolean showIcon = true;


        /**
         * 是否显示名字
         */
        public boolean showName = true;


        /**
         * 是否显示距离
         */
        public boolean showDistance = true;


        /**
         * 是否显示高度差
         */
        public boolean showHeight = true;



        /**
         * 文字缩放
         */
        public float textScale = 1.0F;



        /**
         * 图标大小
         */
        public int iconSize = 16;



        /**
         * 多近距离开始隐藏
         */
        public double fadeStartDistance = 6.0D;



        /**
         * 最大显示距离
         *
         * -1 = 无限距离
         */
        public double hideDistance = 256.0D;



        /**
         * HUD配置检查
         */
        public void validate(){


            /*
             * 文字缩放
             */
            if(Float.isNaN(textScale) || Float.isInfinite(textScale) || textScale < 0.5F || textScale > 3.0F){

                textScale = 1.0F;

            }



            /*
             * 图标大小
             */
            if(iconSize < 1 || iconSize > 64){

                iconSize = 16;

            }



            /*
             * 渐隐距离
             *
             * -1关闭
             */
            if(Double.isNaN(fadeStartDistance) || Double.isInfinite(fadeStartDistance) || fadeStartDistance < -1){

                fadeStartDistance = 6.0D;

            }



            /*
             * 最大显示距离
             *
             * -1无限
             */
            if(Double.isNaN(hideDistance) || Double.isInfinite(hideDistance) || hideDistance < -1){

                hideDistance = 256.0D;

            }
        }
    }




    /*
     * 世界标记配置
     */
    public static class WorldMarker {


        /**
         * 是否启用
         */
        public boolean enabled = true;



        /**
         * 世界标记大小
         */
        public float scale = 1.0F;



        /**
         * 是否开启发光效果
         */
        public boolean glow = true;



        /**
         * 最大渲染距离
         *
         * -1 = 无限距离
         */
        public double renderDistance = 256.0D;



        /**
         * 是否显示名称
         */
        public boolean showName = true;



        /**
         * 是否显示距离
         */
        public boolean showDistance = true;



        /**
         * 名称高度偏移
         */
        public float nameHeight = 1.6F;



        /**
         * 标记点高度偏移
         */
        public float diamondHeight = 2.5F;



        /**
         * 旋转速度
         */
        public float rotationSpeed = 90.0F;



        /**
         * 是否开启上下浮动动画
         */
        public boolean floatAnimation = true;



        /**
         * 浮动高度
         */
        public float floatHeight = 0.25F;




        /**
         * 世界标记配置检查
         */
        public void validate(){



            /*
             * 大小
             */
            if(Float.isNaN(scale)
                    || Float.isInfinite(scale)
                    || scale < 0.1F
                    || scale > 5.0F){

                scale = 1.0F;

            }



            /*
             * 最大距离
             *
             * -1无限
             */
            if(Double.isNaN(renderDistance)
                    || Double.isInfinite(renderDistance)
                    || renderDistance < -1
                    || (renderDistance > 10000
                    && renderDistance != -1)){

                renderDistance = 256.0D;

            }



            /*
             * 名称高度
             */
            if(Float.isNaN(nameHeight)
                    || Float.isInfinite(nameHeight)
                    || nameHeight < 0
                    || nameHeight > 10){

                nameHeight = 1.6F;

            }



            /*
             * 标记点高度
             */
            if(Float.isNaN(diamondHeight)
                    || Float.isInfinite(diamondHeight)
                    || diamondHeight < 0
                    || diamondHeight > 20){

                diamondHeight = 2.5F;

            }



            /*
             * 旋转速度
             */
            if(Float.isNaN(rotationSpeed)
                    || Float.isInfinite(rotationSpeed)
                    || rotationSpeed < 0
                    || rotationSpeed > 360){

                rotationSpeed = 90.0F;

            }



            /*
             * 浮动高度
             */
            if(Float.isNaN(floatHeight)
                    || Float.isInfinite(floatHeight)
                    || floatHeight < 0
                    || floatHeight > 5){

                floatHeight = 0.25F;

            }
        }
    }
}