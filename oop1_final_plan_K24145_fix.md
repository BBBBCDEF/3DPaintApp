#3D描画アプリケーション設計資料

##1. アプリケーションの目的

本アプリケーションは、Java SwingのGUI機能と、3Dグラフィックスの基礎学習および視覚的理解の支援を目的としている。
近年、リアルタイム3D描画や物理ベースレンダリング技術が急速に普及しているため、3Dグラフィックスについて理解を深めることを目的とする。
そこで、簡潔かつ実行しやすいJavaアプリケーションを通じて、以下の課題を解決する：
- ベクトル演算や幾何的交差の基本を可視化
- 光源、視点、陰影の概念の直感的理解
- 物体追加による動的なシーン構成と再描画

(いくつか存在する描画手法の中でも、最も直感的かつわかりやすい、レイトレーシングを用いる)
##2. 機能説明
| 機能名           | 概要                                                                  |
|---------------|---------------------------------------------------------------------|
| レンダリング処理      | レイトレーシングによる3Dシーンの描画                                                 |
| 物体追加機能        | 立方体・三角錐・球をUIから選択し、パラメータを入力してシーンに追加可能                                |
| リアルタイム再描画     | 物体追加時、レンダリング画面が即座に更新                                                |
| 光源の設定（固定）     | シーン内に固定の光源を設け、基本的な陰影（ランバート反射）を計算                                    |
| ベクトル演算ユーティリティ | 法線ベクトルや方向ベクトルの演算、正規化、内積処理など                                         |
| 物体編集・削除機能     | リストから選択して物体を編集・削除可能。ショートカットキーを設定し、簡単に物体の形状変更や、移動ができる                |
| 左上にパフォーマンス表示  | 高負荷な処理のため、FPS、CPU使用率、メモリ使用率を計算し表示する                                 |
| ベンチマーク機能      | 大量のオブジェクトを配置し、全く同じ条件でレンダリングすることでCPU性能やアプリの動作速度を比較可能にする。スコアは自動保存される。 |
| 設定            | フレームレート、レンダリング精度、解像度、背景色などを変更可能にする。設定は自動保存される。                      |
| シーンの保存・読み込み   | .rts形式の、インスタンスをシリアライズしたファイルを保存でき、読み込み、上書き保存が可能。                     |
| ソフト初期化        | 初期設定に戻す                                                             |
| レンダリングスレッド再起動 | 長時間起動していると重くなるため、レンダリングスレッドを再起動可能にすることで負荷軽減。                        |
| レンダリング自動停止    | 他のアプリにフォーカスがある時は、レンダリングを停止し、無駄な処理・CPU発熱量を抑える。                       |
##3. 画面構成説明
###左側：描画キャンバス(DrawingPanel)<br>
- 1ピクセルずつレイを飛ばし、最初に交差したオブジェクトの色を計算
###右側：オブジェクト操作パネル(ControlPanel)<br>
- オブジェクトの種類を選択（立方体、三角錐、球）
- 位置（x, y, z）、サイズ、色などのパラメータを入力
- 「追加」ボタンでシーンにオブジェクトを追加し、再描画 
- リスト一覧で既存のオブジェクトを選び、変更・削除 
###上部のメニューバー
- ファイル、編集、シミュレーション、ベンチマークのメニューを追加し、シーンの保存や読み込み、便利機能などを呼び出せる。
- ショートカットキーにも対応させる。
##4. データ設計と取り扱い
###4.2 データ構成
データ構造を扱う主なクラスを以下に示す。これらのクラスを中心とする。なお、メソッドは一部のみ記載する。
####三次元ベクトルクラス
- 三次元のベクトルを表すクラス
- 加算、減算、乗算や、正規化などのベクトル操作を簡便に行えるよう設計する。
- 扱う際、直感的に処理内容がわかりやすいメソッド名を採用する。
```Java
package app.util;

public class Vector3D {
    final double x, y, z;

    public Vector3D(double x, double y, double z) {
        this.x = x; this.y = y; this.z = z;
    }
    public Vector3D add(Vector3D v) {
        return new Vector3D(x + v.x, y + v.y, z + v.z);
    }
    public Vector3D subtract(Vector3D v) {
        return new Vector3D(x - v.x, y - v.y, z - v.z);
    }
    public Vector3D multiply(double s) {
        return new Vector3D(x * s, y * s, z * s);
    }
    public double dot(Vector3D v) {
        return x * v.x + y * v.y + z * v.z;
    }
    public Vector3D cross(Vector3D v) {
        return new Vector3D(y * v.z - z * v.y, z * v.x - x * v.z, x * v.y - y * v.x);
    }
    public double length() {
        return Math.sqrt(dot(this));
    }
    public Vector3D normalize() {
        double len = length(); return new Vector3D(x / len, y / len, z / len);
    }
    public double distance(Vector3D v) {
        return this.subtract(v).length();
    }
    public Vector3D rotateAroundAxis(Vector3D axis, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        Vector3D v = this;
        return axis.multiply(axis.dot(v) * (1 - cos))
                .add(v.multiply(cos))
                .add(axis.cross(v).multiply(sin));
    }
}
```
####シーンクラス
- 形状や光源、背景色をリストで保持する。
- 光線を入力して衝突結果を返すメソッドを実装する。
```Java
public class Scene {
    private final List<Shape> objects = new ArrayList<>();
    private final List<Light> lights = new ArrayList<>();
    private final Color backgroundColor = Color.BLACK;

    public IntersectionResult findClosestIntersection(Ray ray) {
        IntersectionResult closestHit = IntersectionResult.NO_HIT;
        for(Shape shape : objects) {
            IntersectionResult result = shape.intersect(ray);
            if(result.isHit() && result.getDistance() < closestHit.getDistance()) {
                closestHit = result;
            }
        }
        return closestHit;
    }
}
```
####形状クラス
- 複数種類の形状が存在するため、抽象クラスとする
- 設置できる`Placeable`、レイと衝突できる`RayTraceable`、保存用の`Serializable`を実装する。
- 各オブジェクトはこれを継承
- 平面がある形状(立方体, 三角錐)はShapeを継承した抽象クラスComposedShapeを継承
```Java
public abstract class Shape implements Placeable, Serializable, RayTraceable {
    protected Vector3D center;
    public abstract IntersectionResult intersect(Ray ray);
    public abstract Material getMaterial();
    public abstract double getSize();
    public abstract void setSize(double size);
}
```
####光線(レイ)クラス
- 原点と方向を持ち、光線を表す
```Java
public class Ray {
    final Vector3D origin;
    final Vector3D direction;
}
```
####衝突結果クラス
- 衝突結果を保持する
- 衝突しなかった場合の定数`NO_HIT`を持つ
- 衝突対象の形状、衝突地点、形状を中心とした衝突地点、衝突までの距離を持つ
```Java
public class IntersectionResult {
    public static final IntersectionResult NO_HIT = new IntersectionResult(null, null, null, Double.MAX_VALUE);
    private final Shape shape;
    private final Vector3D hitPoint;
    private final Vector3D normal;
    private final double distance;
}
```
####カメラクラス
- カメラの位置と向きを持つ
- 外部から回転・移動・視点リセットするためのメソッドを用意する
- カメラからディスプレイの各ピクセルに対してレイを照射する`createRay`メソッドを実装する。
```Java
public class Camera {
    private Vector3D position = new Vector3D(0, 0, 5);
    private Vector3D forward = new Vector3D(0, 0, -1);
    private Vector3D up = new Vector3D(0, 1, 0);
    
    public Ray createRay(int x, int y, int width, int height) {
        double fov = 70.0;
        double aspectRatio = (double) width / height;
        double scale = Math.tan(Math.toRadians(fov * 0.5));

        Vector3D right = forward.cross(up).normalize();
        Vector3D trueUp = right.cross(forward).normalize(); // カメラの上方向

        double px = (2 * (x + 0.5) / (double) width - 1) * aspectRatio * scale;
        double py = (1 - 2 * (y + 0.5) / (double) height) * scale;

        Vector3D direction = forward
                .add(right.multiply(px))
                .add(trueUp.multiply(py))
                .normalize();

        return new Ray(position, direction);
    }
}
```
####セーブデータクラス
- シーンを保存するため、形状、カメラ、選択されている色の情報を持つ。
- クラス変更時に安全性を担保するため、`serialVersionUID`というバージョンも一緒に持たせる。
```Java
public class SaveData implements Serializable {
    private long serialVersionUID;
    private Scene scene;
    private Camera camera;
    private Color selectedColor;

    public SaveData(long serialVersionUID, Scene scene, Camera camera, Color selectedColor) {
        this.serialVersionUID = serialVersionUID;
        this.scene = scene;
        this.camera = camera;
        this.selectedColor = selectedColor;
    }
    public long getSerialVersionUID() {
        return serialVersionUID;
    }
    public Scene getScene() {
        return scene;
    }
    public Camera getCamera() {
        return camera;
    }
    public Color getSelectedColor() {
        return selectedColor;
    }
}
```
###4.3 ユーザ入力とバリデーション
- ユーザーの数値入力は`try-catch`により例外処理
- 無効な入力はダイアログでエラーメッセージを表示
- 定期的に全てのオブジェクトとレイの交差を判定し、オブジェクトの変更が即座に反映されるようにする(リアルタイムレイトレーシング)
###4.4 詳細レンダリングアルゴリズム
ここでは、Phongモデルを参考に物体の質感を再現することに注力する。
- ディスプレイをカメラからある程度離して仮想的に置き、カメラからディスプレイの各ピクセルに向かってレイを発射する。
- そのレイを追跡し、物体に当たったかどうかを`InteractionResult`で管理する。
- レイと物体の衝突点に関して、シャドウ、反射、光の当たり具合を計算する。
- 光源の光が衝突点の接面に当たる角度のcosで明るさを決定する。
- 衝突点から少しだけ法線方向にずらした位置から光源に向かってレイを飛ばし、光源までに物体があれば影判定とする。
- `reflectivity`が0より大きいならば、鏡面反射を再起的に計算する。入射ベクトルをI、法線ベクトルをN、反射ベクトルをRとすると、R=I−2(I⋅N)Nと表される。Rを求め、Rの延長線上に物体があれば反射判定とする。
- `reflectivity`によって物体の色と反射光の色を加重平均する。
- 計算結果を元に、ピクセルに色を塗る。
- マルチスレッドで、行ごとに担当するスレッドを分ける。CPUのスレッドはフルで用いる。
- 全てのスレッドの処理が完了すれば、1フレームの描画が完了し、ティアリングを減少させるためバッファーに格納してから表示する。
- ※1フレームにつき700x572=400400本のレイを計算する必要がある。
###5. 拡張性と今後の見通し

| 今後の追加機能案 | 内容                                                       |
|----------|----------------------------------------------------------|
| 屈折の実装    | 透明体の屈折                                                   |
| パストレーシング | さらにリアルにするため、パストレーシングで描画する。極めて重いため、レイトレーシングと切り替えられるようにする。 |
##6. 使用ライブラリ・技術要素

| 技術・ライブラリ         | 用途                       |
|------------------|--------------------------|
| Java Swing       | UI描画・イベント処理              |
| Java 2D Graphics | ピクセル単位の描画（BufferedImage） |
| FlatLaf          | UIの見た目変更                 |
