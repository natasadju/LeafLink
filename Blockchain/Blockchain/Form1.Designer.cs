namespace Blockchain
{
    partial class Form1
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            this.textBox_port = new System.Windows.Forms.TextBox();
            this.btn_connect = new System.Windows.Forms.Button();
            this.btn_mine = new System.Windows.Forms.Button();
            this.btn_connect_to = new System.Windows.Forms.Button();
            this.textBox_connect_to = new System.Windows.Forms.TextBox();
            this.textBox_username = new System.Windows.Forms.TextBox();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.richTextBox_chain = new System.Windows.Forms.RichTextBox();
            this.richTextBox_blocks = new System.Windows.Forms.RichTextBox();
            this.label3 = new System.Windows.Forms.Label();
            this.label4 = new System.Windows.Forms.Label();
            this.label5 = new System.Windows.Forms.Label();
            this.richTextBox_validation = new System.Windows.Forms.RichTextBox();
            this.label6 = new System.Windows.Forms.Label();
            this.label7 = new System.Windows.Forms.Label();
            this.richTextBox_connected_to = new System.Windows.Forms.RichTextBox();
            this.label9 = new System.Windows.Forms.Label();
            this.label10 = new System.Windows.Forms.Label();
            this.label_diff = new System.Windows.Forms.Label();
            this.label_len = new System.Windows.Forms.Label();
            this.SuspendLayout();
            // 
            // textBox_port
            // 
            this.textBox_port.Location = new System.Drawing.Point(91, 60);
            this.textBox_port.Name = "textBox_port";
            this.textBox_port.Size = new System.Drawing.Size(100, 22);
            this.textBox_port.TabIndex = 1;
            this.textBox_port.TextChanged += new System.EventHandler(this.textBox_port_TextChanged);
            // 
            // btn_connect
            // 
            this.btn_connect.Enabled = false;
            this.btn_connect.Location = new System.Drawing.Point(197, 30);
            this.btn_connect.Name = "btn_connect";
            this.btn_connect.Size = new System.Drawing.Size(75, 23);
            this.btn_connect.TabIndex = 1;
            this.btn_connect.Text = "Connect";
            this.btn_connect.UseVisualStyleBackColor = true;
            this.btn_connect.Click += new System.EventHandler(this.btn_connect_Click);
            // 
            // btn_mine
            // 
            this.btn_mine.Enabled = false;
            this.btn_mine.Location = new System.Drawing.Point(197, 59);
            this.btn_mine.Name = "btn_mine";
            this.btn_mine.Size = new System.Drawing.Size(75, 23);
            this.btn_mine.TabIndex = 2;
            this.btn_mine.Text = "Mine";
            this.btn_mine.UseVisualStyleBackColor = true;
            this.btn_mine.Click += new System.EventHandler(this.btn_mine_Click);
            // 
            // btn_connect_to
            // 
            this.btn_connect_to.Enabled = false;
            this.btn_connect_to.Location = new System.Drawing.Point(646, 12);
            this.btn_connect_to.Name = "btn_connect_to";
            this.btn_connect_to.Size = new System.Drawing.Size(75, 23);
            this.btn_connect_to.TabIndex = 3;
            this.btn_connect_to.Text = "Connect";
            this.btn_connect_to.UseVisualStyleBackColor = true;
            this.btn_connect_to.Click += new System.EventHandler(this.btn_connect_to_Click);
            // 
            // textBox_connect_to
            // 
            this.textBox_connect_to.Enabled = false;
            this.textBox_connect_to.Location = new System.Drawing.Point(540, 12);
            this.textBox_connect_to.Name = "textBox_connect_to";
            this.textBox_connect_to.Size = new System.Drawing.Size(100, 22);
            this.textBox_connect_to.TabIndex = 4;
            this.textBox_connect_to.TextChanged += new System.EventHandler(this.textBox_connect_to_TextChanged);
            // 
            // textBox_username
            // 
            this.textBox_username.Location = new System.Drawing.Point(91, 30);
            this.textBox_username.Name = "textBox_username";
            this.textBox_username.Size = new System.Drawing.Size(100, 22);
            this.textBox_username.TabIndex = 0;
            this.textBox_username.TextChanged += new System.EventHandler(this.textBox_username_TextChanged);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(12, 33);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(73, 16);
            this.label1.TabIndex = 6;
            this.label1.Text = "Username:";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(48, 63);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(34, 16);
            this.label2.TabIndex = 7;
            this.label2.Text = "Port:";
            // 
            // richTextBox_chain
            // 
            this.richTextBox_chain.ForeColor = System.Drawing.Color.Green;
            this.richTextBox_chain.HideSelection = false;
            this.richTextBox_chain.Location = new System.Drawing.Point(12, 220);
            this.richTextBox_chain.Name = "richTextBox_chain";
            this.richTextBox_chain.ReadOnly = true;
            this.richTextBox_chain.Size = new System.Drawing.Size(379, 218);
            this.richTextBox_chain.TabIndex = 8;
            this.richTextBox_chain.Text = "";
            this.richTextBox_chain.TextChanged += new System.EventHandler(this.richTextBox_chain.TextChanged);
            // 
            // richTextBox_blocks
            // 
            this.richTextBox_blocks.HideSelection = false;
            this.richTextBox_blocks.Location = new System.Drawing.Point(411, 220);
            this.richTextBox_blocks.Name = "richTextBox_blocks";
            this.richTextBox_blocks.ReadOnly = true;
            this.richTextBox_blocks.Size = new System.Drawing.Size(377, 218);
            this.richTextBox_blocks.TabIndex = 9;
            this.richTextBox_blocks.Text = "";
            this.richTextBox_blocks.TextChanged += new System.EventHandler(this.richTextBox_blocks_TextChanged);
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(459, 17);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(73, 16);
            this.label3.TabIndex = 10;
            this.label3.Text = "Connect to:";
            // 
            // label4
            // 
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(12, 201);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(75, 16);
            this.label4.TabIndex = 11;
            this.label4.Text = "BlockChain";
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(411, 201);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(89, 16);
            this.label5.TabIndex = 12;
            this.label5.Text = "Mining blocks";
            // 
            // richTextBox_validation
            // 
            this.richTextBox_validation.HideSelection = false;
            this.richTextBox_validation.Location = new System.Drawing.Point(12, 113);
            this.richTextBox_validation.Name = "richTextBox_validation";
            this.richTextBox_validation.ReadOnly = true;
            this.richTextBox_validation.Size = new System.Drawing.Size(379, 81);
            this.richTextBox_validation.TabIndex = 13;
            this.richTextBox_validation.Text = "";
            this.richTextBox_validation.TextChanged += new System.EventHandler(this.richTextBox_validation_TextChanged);
            // 
            // label6
            // 
            this.label6.AutoSize = true;
            this.label6.Location = new System.Drawing.Point(12, 91);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(130, 16);
            this.label6.TabIndex = 15;
            this.label6.Text = "Validating new block";
            // 
            // label7
            // 
            this.label7.AutoSize = true;
            this.label7.Location = new System.Drawing.Point(411, 91);
            this.label7.Name = "label7";
            this.label7.Size = new System.Drawing.Size(81, 16);
            this.label7.TabIndex = 16;
            this.label7.Text = "Connections";
            // 
            // richTextBox_connected_to
            // 
            this.richTextBox_connected_to.ForeColor = System.Drawing.Color.Blue;
            this.richTextBox_connected_to.HideSelection = false;
            this.richTextBox_connected_to.Location = new System.Drawing.Point(414, 113);
            this.richTextBox_connected_to.Name = "richTextBox_connected_to";
            this.richTextBox_connected_to.ReadOnly = true;
            this.richTextBox_connected_to.Size = new System.Drawing.Size(374, 81);
            this.richTextBox_connected_to.TabIndex = 17;
            this.richTextBox_connected_to.Text = "";
            this.richTextBox_connected_to.TextChanged += new System.EventHandler(this.richTextBox_connected_to_TextChanged);
            // 
            // label9
            // 
            this.label9.AutoSize = true;
            this.label9.Location = new System.Drawing.Point(308, 33);
            this.label9.Name = "label9";
            this.label9.Size = new System.Drawing.Size(102, 16);
            this.label9.TabIndex = 19;
            this.label9.Text = "Current difficulty:";
            // 
            // label10
            // 
            this.label10.AutoSize = true;
            this.label10.Location = new System.Drawing.Point(308, 63);
            this.label10.Name = "label10";
            this.label10.Size = new System.Drawing.Size(115, 16);
            this.label10.TabIndex = 20;
            this.label10.Text = "Blockchain length:";
            // 
            // label_diff
            // 
            this.label_diff.AutoSize = true;
            this.label_diff.ForeColor = System.Drawing.Color.Black;
            this.label_diff.Location = new System.Drawing.Point(418, 33);
            this.label_diff.Name = "label_diff";
            this.label_diff.Size = new System.Drawing.Size(14, 16);
            this.label_diff.TabIndex = 21;
            this.label_diff.Text = "0";
            this.label_diff.Click += new System.EventHandler(this.label11_Click);
            // 
            // label_len
            // 
            this.label_len.AutoSize = true;
            this.label_len.ForeColor = System.Drawing.Color.Blue;
            this.label_len.Location = new System.Drawing.Point(430, 62);
            this.label_len.Name = "label_len";
            this.label_len.Size = new System.Drawing.Size(14, 16);
            this.label_len.TabIndex = 22;
            this.label_len.Text = "0";
            // 
            // Form1
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(8F, 16F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(800, 450);
            this.Controls.Add(this.label_len);
            this.Controls.Add(this.label_diff);
            this.Controls.Add(this.label10);
            this.Controls.Add(this.label9);
            this.Controls.Add(this.richTextBox_connected_to);
            this.Controls.Add(this.label7);
            this.Controls.Add(this.label6);
            this.Controls.Add(this.richTextBox_validation);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.richTextBox_blocks);
            this.Controls.Add(this.richTextBox_chain);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.textBox_username);
            this.Controls.Add(this.textBox_connect_to);
            this.Controls.Add(this.btn_connect_to);
            this.Controls.Add(this.btn_mine);
            this.Controls.Add(this.btn_connect);
            this.Controls.Add(this.textBox_port);
            this.Name = "Form1";
            this.Text = "Form1";
            this.Load += new System.EventHandler(this.Form1_Load);
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.TextBox textBox_port;
        private System.Windows.Forms.Button btn_connect;
        private System.Windows.Forms.Button btn_mine;
        private System.Windows.Forms.Button btn_connect_to;
        private System.Windows.Forms.TextBox textBox_connect_to;
        private System.Windows.Forms.TextBox textBox_username;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.RichTextBox richTextBox_chain;
        private System.Windows.Forms.RichTextBox richTextBox_blocks;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.RichTextBox richTextBox_validation;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.Label label7;
        private System.Windows.Forms.RichTextBox richTextBox_connected_to;
        private System.Windows.Forms.Label label9;
        private System.Windows.Forms.Label label10;
        private System.Windows.Forms.Label label_diff;
        private System.Windows.Forms.Label label_len;
    }
}

