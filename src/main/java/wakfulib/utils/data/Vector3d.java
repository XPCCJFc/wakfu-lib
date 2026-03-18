package wakfulib.utils.data;

public class Vector3d {
    private double m_x;
    private double m_y;
    private double m_z;

    public Vector3d(double x, double y, double z) {
        this.m_x = x;
        this.m_y = y;
        this.m_z = z;
    }

    public double dot(Vector3d v) {
        return this.m_x * v.m_x + this.m_y * v.m_y + this.m_z * v.m_z;
    }

    public void mulCurrent(double scale) {
        this.m_x *= scale;
        this.m_y *= scale;
        this.m_z *= scale;
    }

    public Vector3d mul(double s) {
        return new Vector3d(s * this.m_x, s * this.m_y, s * this.m_z);
    }

    public Vector3d normalize() {
        double l = this.length();
        if (l == 0.0) {
            l = 1.0E-7;
        }
        return this.mul(1.0 / l);
    }

    public void normalizeCurrent() {
        double l = this.length();
        if (l == 0.0) {
            l = 1.0E-7;
        }
        this.mulCurrent(1.0 / l);
    }

    public double length() {
        double l = this.m_x * this.m_x + this.m_y * this.m_y + this.m_z * this.m_z;
        if (l == 0.0) {
            l = 1.0E-7;
        }
        return Math.sqrt(l);
    }
}
