module Paperclip
  class Attachment
    class << self
      alias_method :old_default_options, :default_options
    end
    
    def self.default_options
      @default_options ||= old_default_options.merge(
        :url           => "/system/:class/:attachment/:id/:style/:basename.:extension",
        :path          => ":rails_root/public/system/:class/:attachment/:id/:style/:basename.:extension",
        :default_url   => "/missing/:style/:class/:attachment.png"
      )
    end
  end
end